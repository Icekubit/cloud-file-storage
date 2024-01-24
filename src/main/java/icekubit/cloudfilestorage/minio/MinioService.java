package icekubit.cloudfilestorage.minio;

import icekubit.cloudfilestorage.dto.MinioItemDto;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MinioService {
    private final MinioClient minioClient;
    private final String DEFAULT_BUCKET_NAME = "user-files";

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    @SneakyThrows
    private void createDefaultBucket() {
        boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket("user-files").build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket("user-files").build());
            log.info("Bucket 'user-files' was created");
        } else {
            log.info("Can't create bucket 'user-files' because bucket with this name already exists");
        }
    }

    @SneakyThrows
    public void createFolder(String minioPathToFolder) {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("user-files")
                        .object(minioPathToFolder)
                        .stream(
                                new ByteArrayInputStream(new byte[] {}), 0, -1)
                        .build());
    }

    public void uploadMultipartFile(String minioPathToFile, MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("user-files")
                            .object(minioPathToFile)
                            .stream(
                                    file.getInputStream(), -1, 10485760)
                            .build());
            log.info("The file " + minioPathToFile + " is successfully added to the bucket " + DEFAULT_BUCKET_NAME);
        } catch (ErrorResponseException e) {
            throw new RuntimeException(e);
        } catch (InsufficientDataException e) {
            throw new RuntimeException(e);
        } catch (InternalException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (ServerException e) {
            throw new RuntimeException(e);
        } catch (XmlParserException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean doesFolderExist(String path, int userId) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .prefix(getDirNameByUserId(userId) + path)
                        .build());
        if (results.iterator().hasNext()) {
            try {
                return results.iterator().next().get().isDir();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public List<MinioItemDto> getListOfItems(String path, int userId) {
        String prefix = path.isEmpty() ? getDirNameByUserId(userId) : getDirNameByUserId(userId) + path + "/";
        var results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .prefix(prefix)
                        .build());
        List<MinioItemDto> listOfItems = new ArrayList<>();
        for (Result<Item> result: results) {
            try {
                // skip if resource is source folder to exclude it from listOfItems
                if (prefix.equals(result.get().objectName())) {
                    continue;
                }
                listOfItems.add(convertMinioItemToDto(result.get()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return listOfItems;
    }

    public List<MinioItemDto> getListOfItems(String absolutePath) {
        var results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .prefix(absolutePath)
                        .build());
        List<MinioItemDto> listOfItems = new ArrayList<>();
        for (Result<Item> result: results) {
            try {
                // skip if resource is source folder to exclude it from listOfItems
                if (absolutePath.equals(result.get().objectName())) {
                    continue;
                }
                listOfItems.add(convertMinioItemToDto(result.get()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return listOfItems;
    }

    public void removeObject(String itemForDeleting, Integer userId) {
        String minioPathToObject = getDirNameByUserId(userId) + itemForDeleting;
        if (minioPathToObject.endsWith("/")) {
            var results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(DEFAULT_BUCKET_NAME)
                            .prefix(minioPathToObject)
                            .build());
            List<MinioItemDto> listOfItems = new ArrayList<>();
            for (Result<Item> result : results) {
                try {
                    // skip if resource is source folder to exclude it from listOfItems
                    if (minioPathToObject.equals(result.get().objectName())) {
                        continue;
                    }
                    listOfItems.add(convertMinioItemToDto(result.get()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            for (MinioItemDto item: listOfItems) {
                removeObject(item.getRelativePath(), userId);
            }
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(DEFAULT_BUCKET_NAME)
                            .object(minioPathToObject)
                            .build());
            log.info("The object " + minioPathToObject + " is removed successfully");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void renameObject(String relativePathToObject, String newObjectName, Integer userId) {
        String newRelativePathToObject = getNewRelativePathToObject(relativePathToObject, newObjectName);

        if (relativePathToObject.endsWith("/")) {
            String newFolder = getDirNameByUserId(userId) + newRelativePathToObject;
            String sourceFolder = getDirNameByUserId(userId) + relativePathToObject;

            createFolder(newFolder);

            copyItems(sourceFolder, newFolder);

        } else {

            try {
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(DEFAULT_BUCKET_NAME)
                                .object(getDirNameByUserId(userId) + newRelativePathToObject)
                                .source(
                                        CopySource.builder()
                                                .bucket(DEFAULT_BUCKET_NAME)
                                                .object(getDirNameByUserId(userId) + relativePathToObject)
                                                .build())
                                .build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        removeObject(relativePathToObject, userId);
    }

    private void copyItems(String sourceFolder, String newFolder) {
        var listOfItems = getListOfItems(sourceFolder);
        for (MinioItemDto item: listOfItems) {
            if (item.getIsDirectory()) {
                String pathToNewFolder = newFolder + item.getPath().substring(sourceFolder.length());
                createFolder(pathToNewFolder);
                copyItems(item.getPath(), pathToNewFolder);
            } else {
                try {
                    String destinationPath = newFolder + item.getPath().substring(sourceFolder.length());
                    minioClient.copyObject(
                            CopyObjectArgs.builder()
                                    .bucket(DEFAULT_BUCKET_NAME)
                                    .object(destinationPath)
                                    .source(
                                            CopySource.builder()
                                                    .bucket(DEFAULT_BUCKET_NAME)
                                                    .object(item.getPath())
                                                    .build())
                                    .build());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @NotNull
    private String getNewRelativePathToObject(String relativePathToObject, String newObjectName) {
        String newRelativePathToObject = "";
        if (relativePathToObject.endsWith("/")) {
            newRelativePathToObject = relativePathToObject.substring(0, relativePathToObject.length() - 1);
            if (newRelativePathToObject.contains("/")) {
                newRelativePathToObject = newRelativePathToObject.substring(0, newRelativePathToObject.lastIndexOf("/") + 1);
            } else {
                newRelativePathToObject = "";
            }
            newRelativePathToObject = newRelativePathToObject + newObjectName + "/";
        } else {
            if (relativePathToObject.contains("/")) {
                newRelativePathToObject = relativePathToObject.substring(0, relativePathToObject.lastIndexOf("/") + 1);
            } else {
                newRelativePathToObject = "";
            }
            newRelativePathToObject = newRelativePathToObject + newObjectName;
        }
        return newRelativePathToObject;
    }

    private String getDirNameByUserId(Integer userId) {
        return "user-" + userId + "-files/";
    }

    private MinioItemDto convertMinioItemToDto(Item item) {
        MinioItemDto result = new MinioItemDto();
        result.setIsDirectory(item.isDir());
        result.setPath(item.objectName());
        String path = item.objectName();
        result.setRelativePath(path.substring(path.indexOf("-files") + 7));
        return result;
    }
}
