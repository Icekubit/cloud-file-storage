package icekubit.cloudfilestorage.minio;

import icekubit.cloudfilestorage.dto.MinioItemDto;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
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

    @SneakyThrows
    public void uploadFile(String pathToFile, String minioPathToFile) {
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("user-files")
                        .object(minioPathToFile)
                        .filename(pathToFile)
                        .build());
        log.info(pathToFile + " is successfully uploaded as object 'just_random_name' to bucket 'user-files'.");
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
