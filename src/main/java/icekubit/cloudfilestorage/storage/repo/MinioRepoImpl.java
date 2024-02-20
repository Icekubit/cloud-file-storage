package icekubit.cloudfilestorage.storage.repo;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MinioRepoImpl implements MinioRepo {

    private final MinioClient minioClient;
    private final String DEFAULT_BUCKET_NAME = "user-files";

    public MinioRepoImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    private void createDefaultBucket() {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(DEFAULT_BUCKET_NAME).build());
            log.info("Bucket '" + DEFAULT_BUCKET_NAME + "' was created");
        } catch (ErrorResponseException e) {
            log.error("Can't create bucket '" + DEFAULT_BUCKET_NAME + "' because bucket with this name already exists");
        } catch (InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException
                 | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("The exception was caught: " + e.getMessage());
        }

    }

    @Override
    public void createFolder(String path) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(DEFAULT_BUCKET_NAME)
                            .object(path)
                            .stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
            log.info("The folder '" + path + "' is created");
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                 | InvalidKeyException | InvalidResponseException | IOException
                 | NoSuchAlgorithmException | ServerException | XmlParserException e) {
                log.error("The exception was caught: " + e.getMessage());
        }
    }

    @Override
    public void uploadFile(MultipartFile file, String destination) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(DEFAULT_BUCKET_NAME)
                            .object(destination)
                            .stream(
                                    file.getInputStream(), -1, 10485760)
                            .build());
            log.info("The file " + destination + " is successfully added to the bucket " + DEFAULT_BUCKET_NAME);
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                 | InvalidKeyException | InvalidResponseException | IOException
                 | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("The exception was caught: " + e.getMessage());
        }

    }

    @Override
    public InputStream downloadFile(String path) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(DEFAULT_BUCKET_NAME)
                            .object(path)
                            .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                 | InvalidKeyException | InvalidResponseException | IOException
                 | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("The exception was caught: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void copyFile(String source, String destination) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(DEFAULT_BUCKET_NAME)
                            .object(destination)
                            .source(
                                    CopySource.builder()
                                            .bucket(DEFAULT_BUCKET_NAME)
                                            .object(source)
                                            .build())
                            .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                 | InvalidKeyException | InvalidResponseException | IOException
                 | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("The exception was caught: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void copyFolder(String source, String destination) {
        List<Item> listOfItems = getListOfItems(source);
        for (Item item: listOfItems) {
            if (item.isDir()) {
                String pathToNewFolder = destination + item.objectName().substring(source.length());
                createFolder(pathToNewFolder);
                copyFolder(item.objectName(), pathToNewFolder);
            } else {
                String destinationPath = destination + item.objectName().substring(source.length());
                copyFile(item.objectName(), destinationPath);
            }
        }
    }

    @Override
    public void removeObject(String path) {
        if (path.endsWith("/")) {
            List<Item> listOfItems = getListOfItems(path);
            for (Item item: listOfItems) {
                removeObject(item.objectName());
            }
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(DEFAULT_BUCKET_NAME)
                            .object(path)
                            .build());
            log.info("The object " + path + " is removed successfully");
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                 | InvalidKeyException | InvalidResponseException | IOException
                 | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("The exception was caught: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void renameObject(String path, String newObjectName) {
        if (path.endsWith("/")) {
            String newFolder = Paths.get(path).getParent().toString() + "/" + newObjectName + "/";
            createFolder(newFolder);
            copyFolder(path, newFolder);
        } else {
            String minioPathToRenamedObject = Paths.get(path).getParent().toString() + "/" + newObjectName;
            copyFile(path, minioPathToRenamedObject);
        }
        removeObject(path);
    }

    @Override
    public Boolean doesFolderExist(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .prefix(path)
                        .build());
        try {
            for (Result<Item> result : results) {
                if (result.get().objectName().startsWith(path)) {
                    return true;
                }
            }
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                 | InvalidKeyException | InvalidResponseException | IOException
                 | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("The exception was caught: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return false;
    }

    @Override
    public Boolean doesFileExist(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .prefix(path)
                        .build());
        try {
            for (Result<Item> result : results) {
                if (result.get().objectName().startsWith(path)) {
                    return true;
                }
            }
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                 | InvalidKeyException | InvalidResponseException | IOException
                 | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("The exception was caught: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return false;
    }

    @Override
    public List<Item> getListOfItems(String path) {
        var results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .prefix(path)
                        .build());
        List<Item> listOfItemsExcludingParentFolder = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                if (result.get().objectName().equals(path))
                    continue;
                listOfItemsExcludingParentFolder.add(result.get());
            }
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                 | InvalidKeyException | InvalidResponseException | IOException
                 | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("The exception was caught: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return listOfItemsExcludingParentFolder;
    }

    @Override
    public List<Item> getListOfItemsRecursively(String path) {
        List<Item> listOfItems = getListOfItems(path);
        List<Item> result = new ArrayList<>();
        for (Item item: listOfItems) {
            result.add(item);
            if (item.isDir()) {
                result.addAll(getListOfItemsRecursively(item.objectName()));
            }
        }
        return result;
    }
}
