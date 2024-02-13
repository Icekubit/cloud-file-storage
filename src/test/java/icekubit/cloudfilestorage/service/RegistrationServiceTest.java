package icekubit.cloudfilestorage.service;


import icekubit.cloudfilestorage.auth.exception.UniqueEmailConstraintException;
import icekubit.cloudfilestorage.auth.exception.UniqueNameConstraintException;
import icekubit.cloudfilestorage.auth.service.RegistrationService;
import icekubit.cloudfilestorage.storage.repo.MinioRepo;
import icekubit.cloudfilestorage.storage.service.MinioService;
import icekubit.cloudfilestorage.auth.model.dto.UserDto;
import icekubit.cloudfilestorage.auth.model.entity.User;
import icekubit.cloudfilestorage.auth.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
@Transactional
public class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgresContainer
            = new PostgreSQLContainer<>("postgres:latest");

    @MockBean
    private MinioService minioService;

    @MockBean
    private MinioRepo minioRepo;

    private final UserDto testUser = UserDto.builder()
            .name("test-user")
            .password("test-password")
            .email("test@gmail.com")
            .build();

    @Test
    void databaseContainsUserAfterRegistration() {
        registrationService.registerNewUser(testUser);
        Optional<User> userOptional = userRepository.findByName(testUser.getName());
        assertThat(userOptional).isNotEmpty();
    }

    @Test
    void registrationUserWithTheSameNameThrowsException() {
        registrationService.registerNewUser(testUser);
        String username = testUser.getName();
        UserDto anotherUserWithTheSameName = UserDto.builder()
                .name(username)
                .password("somePassword")
                .email("testUserNamesake@gmail.com")
                .build();
        assertThatThrownBy(() -> registrationService.registerNewUser(anotherUserWithTheSameName))
                .isInstanceOf(UniqueNameConstraintException.class);
    }

    @Test
    void registrationUserWithTheSameEmailThrowsException() {
        registrationService.registerNewUser(testUser);
        UserDto testUserWithTheSameEmail = UserDto.builder()
                .name("testUserWithTheSameEmail")
                .password("somePassword")
                .email(testUser.getEmail())
                .build();
        assertThatThrownBy(() -> registrationService.registerNewUser(testUserWithTheSameEmail))
                .isInstanceOf(UniqueEmailConstraintException.class);
    }
}
