package icekubit.cloudfilestorage.integration;

import icekubit.cloudfilestorage.TestBeans;
import icekubit.cloudfilestorage.exception.UniqueEmailConstraintException;
import icekubit.cloudfilestorage.exception.UniqueNameConstraintException;
import icekubit.cloudfilestorage.model.dto.UserDto;
import icekubit.cloudfilestorage.model.entity.User;
import icekubit.cloudfilestorage.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest(classes = TestBeans.class)
@TestPropertySource("classpath:test-application.properties")
@Transactional
public class RegistrationServiceTest {

    @Autowired
    private icekubit.cloudfilestorage.service.RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void databaseContainsUserAfterRegistration() {
        UserDto testUser = UserDto.builder()
                .name("test")
                .password("test")
                .email("test@gmail.com")
                .build();
        registrationService.registerNewUser(testUser);
        Optional<User> userOptional = userRepository.findByName("test");
        assertThat(userOptional).isNotEmpty();
    }

    @Test
    void registrationUserWithTheSameNameThrowsException() {
        UserDto testUser = UserDto.builder()
                .name("testUser")
                .password("test")
                .email("testUser@gmail.com")
                .build();
        UserDto testUserNamesake = UserDto.builder()
                .name("testUser")
                .password("somePassword")
                .email("testUserNamesake@gmail.com")
                .build();
        registrationService.registerNewUser(testUser);
        assertThatThrownBy(() -> registrationService.registerNewUser(testUserNamesake))
                .isInstanceOf(UniqueNameConstraintException.class);
    }

    @Test
    void registrationUserWithTheSameEmailThrowsException() {
        UserDto testUser = UserDto.builder()
                .name("testUser")
                .password("test")
                .email("testUser@gmail.com")
                .build();
        UserDto testUserWithTheSameEmail = UserDto.builder()
                .name("testUserWithTheSameEmail")
                .password("somePassword")
                .email("testUser@gmail.com")
                .build();
        registrationService.registerNewUser(testUser);
        assertThatThrownBy(() -> registrationService.registerNewUser(testUserWithTheSameEmail))
                .isInstanceOf(UniqueEmailConstraintException.class);
    }
}
