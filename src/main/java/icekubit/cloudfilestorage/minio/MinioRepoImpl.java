package icekubit.cloudfilestorage.minio;

import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
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
    @SneakyThrows
    private void createDefaultBucket() {
        boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(DEFAULT_BUCKET_NAME).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(DEFAULT_BUCKET_NAME).build());
            log.info("Bucket '" + DEFAULT_BUCKET_NAME + "' was created");
        } else {
            log.info("Can't create bucket '" + DEFAULT_BUCKET_NAME + "' because bucket with this name already exists");
        }
    }

    @Override
    @SneakyThrows
    public void createFolder(String path) {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .object(path)
                        .stream(
                                new ByteArrayInputStream(new byte[] {}), 0, -1)
                        .build());
        log.info("The folder '" + path + "' is created");
    }

    @Override
    @SneakyThrows
    public void uploadFile(MultipartFile file, String destination) {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .object(destination)
                        .stream(
                                file.getInputStream(), -1, 10485760)
                        .build());
        log.info("The file " + destination + " is successfully added to the bucket " + DEFAULT_BUCKET_NAME);
    }

    @Override
    @SneakyThrows
    public void copyFile(String source, String destination) {
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
    @SneakyThrows
    public void removeObject(String path) {
        if (path.endsWith("/")) {
            List<Item> listOfItems = getListOfItems(path);
            for (Item item: listOfItems) {
                removeObject(item.objectName());
            }
        }
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .object(path)
                        .build());
        log.info("The object " + path + " is removed successfully");
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
    @SneakyThrows
    public Boolean doesFolderExist(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .prefix(path)
                        .build());
        for (Result<Item> result: results) {
            if (result.get().objectName().startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SneakyThrows
    public List<Item> getListOfItems(String path) {
        var results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .prefix(path)
                        .build());
        List<Item> listOfItemsExcludingParentFolder = new ArrayList<>();
        for (Result<Item> result: results) {
            if (result.get().objectName().equals(path))
                continue;
            listOfItemsExcludingParentFolder.add(result.get());
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
