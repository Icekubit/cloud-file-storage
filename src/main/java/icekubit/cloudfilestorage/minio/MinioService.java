package icekubit.cloudfilestorage.minio;

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
    public void createFolder(String folderName) {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("user-files")
                        .object(folderName)
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

    @SneakyThrows
    public boolean isPathValid(String path, int userId) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(DEFAULT_BUCKET_NAME)
                        .prefix("user-" + userId + "-files/" + path)
                        .build());
            for (Result<Item> result : results) {
                System.out.println(result.get().objectName());
            }
        return results.iterator().hasNext();
    }
}
