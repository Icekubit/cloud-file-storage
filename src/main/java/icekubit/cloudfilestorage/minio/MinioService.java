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
import java.nio.file.Paths;
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
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(DEFAULT_BUCKET_NAME).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(DEFAULT_BUCKET_NAME).build());
            log.info("Bucket '" + DEFAULT_BUCKET_NAME + "' was created");
        } else {
            log.info("Can't create bucket '" + DEFAULT_BUCKET_NAME + "' because bucket with this name already exists");
        }
    }

    @SneakyThrows
    public void createFolder(String minioPathToFolder) {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .object(minioPathToFolder)
                        .stream(
                                new ByteArrayInputStream(new byte[] {}), 0, -1)
                        .build());
        log.info("The folder '" + minioPathToFolder + "' is created");
    }

    @SneakyThrows
    public void uploadMultipartFile(String minioPathToFile, MultipartFile file) {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .object(minioPathToFile)
                        .stream(
                                file.getInputStream(), -1, 10485760)
                        .build());
        log.info("The file " + minioPathToFile + " is successfully added to the bucket " + DEFAULT_BUCKET_NAME);
    }

    @SneakyThrows
    public boolean doesFolderExist(String minioPathToFile) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .prefix(minioPathToFile)
                        .build());
        for (Result<Item> result: results) {
            if (result.get().objectName().startsWith(minioPathToFile)) {
                    return true;
            }
        }
        return false;
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

        String minioPathToFolder = Paths.get(minioPathToObject).getParent().toString() + "/";
        if (getListOfItems(minioPathToFolder).isEmpty()) {
            createFolder(minioPathToFolder);
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
