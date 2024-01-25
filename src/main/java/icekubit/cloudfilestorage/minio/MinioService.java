package icekubit.cloudfilestorage.minio;

import icekubit.cloudfilestorage.dto.MinioItemDto;
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

    public List<MinioItemDto> getListOfItems(String minioPathToFolder) {
        var results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .prefix(minioPathToFolder)
                        .build());
        List<MinioItemDto> listOfItems = new ArrayList<>();
        for (Result<Item> result: results) {
            try {
                // skip if result is source folder to exclude it from listOfItems
                if (minioPathToFolder.equals(result.get().objectName())) {
                    continue;
                }
                listOfItems.add(convertMinioItemToDto(result.get()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return listOfItems;
    }

    public void removeObject(String minioPathToObject) {
        if (minioPathToObject.endsWith("/")) {
            List<MinioItemDto> listOfItems = getListOfItems(minioPathToObject);
            for (MinioItemDto item: listOfItems) {
                removeObject(item.getPath());
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

    public void renameObject(String minioPathToObject, String newObjectName) {
        if (minioPathToObject.endsWith("/")) {
            String newFolder = Paths.get(minioPathToObject).getParent().toString() + "/" + newObjectName + "/";
            createFolder(newFolder);
            copyFolder(minioPathToObject, newFolder);
        } else {
            String minioPathToRenamedObject = Paths.get(minioPathToObject).getParent().toString() + "/" + newObjectName;
            copyFile(minioPathToObject, minioPathToRenamedObject);
        }
        removeObject(minioPathToObject);
    }

    public List<MinioItemDto> searchObjects(String minioPathToRootFolder, String query) {
        List<MinioItemDto> allUserItems = getListOfItemsRecursively(minioPathToRootFolder);
        List<MinioItemDto> foundObjects = new ArrayList<>();
        for (MinioItemDto item: allUserItems) {
            String fileName = Paths.get(item.getPath()).getFileName().toString();
            if (fileName.contains(query)) {
                foundObjects.add(item);
            }
        }
        return foundObjects;
    }

    private List<MinioItemDto> getListOfItemsRecursively(String minioPathToFolder) {
        List<MinioItemDto> listOfItems = getListOfItems(minioPathToFolder);
        List<MinioItemDto> result = new ArrayList<>();
        for (MinioItemDto item: listOfItems) {
            result.add(item);
            if (item.getIsDirectory()) {
                result.addAll(getListOfItemsRecursively(item.getPath()));
            }
        }
        return result;
    }

    private void copyFolder(String sourceFolder, String newFolder) {
        var listOfItems = getListOfItems(sourceFolder);
        for (MinioItemDto item: listOfItems) {
            if (item.getIsDirectory()) {
                String pathToNewFolder = newFolder + item.getPath().substring(sourceFolder.length());
                createFolder(pathToNewFolder);
                copyFolder(item.getPath(), pathToNewFolder);
            } else {
                String destinationPath = newFolder + item.getPath().substring(sourceFolder.length());
                copyFile(item.getPath(), destinationPath);
            }
        }
    }

    private void copyFile(String sourceFile, String newFile) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(DEFAULT_BUCKET_NAME)
                            .object(newFile)
                            .source(
                                    CopySource.builder()
                                            .bucket(DEFAULT_BUCKET_NAME)
                                            .object(sourceFile)
                                            .build())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
