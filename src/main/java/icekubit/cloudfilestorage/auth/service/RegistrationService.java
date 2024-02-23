package icekubit.cloudfilestorage.auth.service;

import icekubit.cloudfilestorage.auth.exception.UniqueEmailConstraintException;
import icekubit.cloudfilestorage.auth.exception.UniqueNameConstraintException;
import icekubit.cloudfilestorage.storage.service.MinioService;
import icekubit.cloudfilestorage.auth.model.dto.UserDto;
import icekubit.cloudfilestorage.auth.model.entity.User;
import icekubit.cloudfilestorage.auth.repo.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RegistrationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final MinioService minioService;

    private String nameConstraint;
    private String emailConstraint;

    public RegistrationService(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               MinioService minioService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.minioService = minioService;
    }

    @PostConstruct
    private void initialize() {
        nameConstraint = userRepository.findConstraintNameByColumnName("name");
        emailConstraint = userRepository.findConstraintNameByColumnName("email");
    }

    public void registerNewUser(UserDto userDto) {
        User newUser = User.builder()
                .name(userDto.getName())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .email(userDto.getEmail())
                .build();
        try {
            userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains(nameConstraint)) {
                throw new UniqueNameConstraintException("The user with username "
                        + newUser.getName() + " already exists");
            }

            if (e.getMessage().contains(emailConstraint)) {
                throw new UniqueEmailConstraintException("The user with email "
                        + newUser.getName() + " already exists");
            }
        }
        log.info("User was added to database: " + userDto);
        minioService.createRootFolder(newUser.getId());
    }
}
