package icekubit.cloudfilestorage.integration;

import icekubit.cloudfilestorage.minio.MinioRepo;
import icekubit.cloudfilestorage.minio.MinioService;
import icekubit.cloudfilestorage.model.dto.UserDto;
import icekubit.cloudfilestorage.repo.UserRepository;
import icekubit.cloudfilestorage.service.RegistrationService;
import io.minio.MinioClient;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
@Transactional
public class MinioServiceTest {


    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgresContainer
            = new PostgreSQLContainer<>("postgres:latest");


    @Container
    private static final GenericContainer<?> minioContainer
            = new GenericContainer<>("quay.io/minio/minio")
            .withExposedPorts(9000)
            .withCommand("server", "/data")
            .withEnv("MINIO_ACCESS_KEY", "username")
            .withEnv("MINIO_SECRET_KEY", "password");

    @DynamicPropertySource
    static void minIOProperties(DynamicPropertyRegistry registry) {
        String minioHost = minioContainer.getHost();
        Integer minioPort = minioContainer.getMappedPort(9000);
        String minioEndpoint = "http://" + minioHost + ":" + minioPort;
        registry.add("minio.endpoint", () -> minioEndpoint);
    }

    @Autowired
    protected RegistrationService registrationService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected MinioService minioService;

    @Autowired
    protected MinioRepo minioRepo;

    @Autowired
    protected MinioClient minioClient;
    private final UserDto testUser = UserDto.builder()
            .name("test-user")
            .password("test-password")
            .email("test@gmail.com")
            .build();

    @Test
    void shouldCreateUserRootFolderWhenUserRegisters() {
        registrationService.registerNewUser(testUser);
        Integer testUserId = userRepository.findByName(testUser.getName()).get().getId();
        String pathToUserRootFolder = "user-" + testUserId + "-files/";
        assertThat(minioRepo.doesFolderExist(pathToUserRootFolder)).isTrue();
    }

    @Test
    @SneakyThrows
    void shouldRenameFileSuccessfully() {
        registrationService.registerNewUser(testUser);
        Integer testUserId = userRepository.findByName(testUser.getName()).get().getId();
        Path testFilePath = Paths.get("src/test/resources/files/test-image.jpg");

        byte[] originalFileContent = Files.readAllBytes(testFilePath);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                testFilePath.getFileName().toString(),
                "multipart/form-data",
                originalFileContent
        );

        minioService.uploadMultipartFile(testUserId, "", multipartFile);

        minioService.renameObject(testUserId, testFilePath.getFileName().toString(), "newFileName");

        List<String> listOfObjectNames = new ArrayList<>();
        for (Item item: minioService.getListOfItems(testUserId, "")) {
            listOfObjectNames.add(item.objectName());
        }

        String oldObjectName = "user-" + testUserId + "-files/" + testFilePath.getFileName();
        String newObjectName = "user-" + testUserId + "-files/" + "newFileName";

        assertThat(listOfObjectNames).doesNotContain(oldObjectName);
        assertThat(listOfObjectNames).contains(newObjectName);

    }

    @Test
    @SneakyThrows
    void shouldDeleteFileSuccessfully() {
        registrationService.registerNewUser(testUser);
        Integer testUserId = userRepository.findByName(testUser.getName()).get().getId();
        Path testFilePath = Paths.get("src/test/resources/files/test-image.jpg");

        byte[] originalFileContent = Files.readAllBytes(testFilePath);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                testFilePath.getFileName().toString(),
                "multipart/form-data",
                originalFileContent
        );

        minioService.uploadMultipartFile(testUserId, "", multipartFile);

        assertThat(minioService.getListOfItems(testUserId, "")).isNotEmpty();

        minioService.removeObject(testUserId, testFilePath.getFileName().toString());

        assertThat(minioService.getListOfItems(testUserId, "")).isEmpty();
    }

    @Test
    @SneakyThrows
    void shouldNotFindAnotherUserFilesWhenSearching() {
        UserDto firstUser = UserDto.builder()
                .name("first-user")
                .password("test-password")
                .email("firstUser@gmail.com")
                .build();

        UserDto secondUser = UserDto.builder()
                .name("second-user")
                .password("test-password")
                .email("secondUser@gmail.com")
                .build();

        registrationService.registerNewUser(firstUser);
        registrationService.registerNewUser(secondUser);

        Integer firstUserId = userRepository.findByName(firstUser.getName()).get().getId();
        Integer secondUserId = userRepository.findByName(secondUser.getName()).get().getId();

        Path pathToFirstUserFile = Paths.get("src/test/resources/files/test-image.jpg");
        Path pathToSecondUserFile = Paths.get("src/test/resources/files/testfile.txt");

        MockMultipartFile firstUserMultipartFile = new MockMultipartFile(
                "file",
                pathToFirstUserFile.getFileName().toString(),
                "multipart/form-data",
                Files.readAllBytes(pathToFirstUserFile)
        );

        MockMultipartFile secondUserMultipartFile = new MockMultipartFile(
                "file",
                pathToSecondUserFile.getFileName().toString(),
                "multipart/form-data",
                Files.readAllBytes(pathToSecondUserFile)
        );

        minioService.uploadMultipartFile(firstUserId, "", firstUserMultipartFile);
        minioService.uploadMultipartFile(secondUserId, "", secondUserMultipartFile);

        List<Item> foundObjects = minioService.searchObjects(firstUserId, "test");
        List<String> foundObjectNames = new ArrayList<>();
        for (Item item: foundObjects) {
            foundObjectNames.add(item.objectName());
        }

        String firstUserObjectName = "user-" + firstUserId + "-files/" + pathToFirstUserFile.getFileName();
        String secondUserObjectName = "user-" + secondUserId + "-files/" + pathToSecondUserFile.getFileName();

        assertThat(foundObjectNames).contains(firstUserObjectName);
        assertThat(foundObjectNames).doesNotContain(secondUserObjectName);


    }
}
