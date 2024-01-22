package icekubit.cloudfilestorage.service;

import icekubit.cloudfilestorage.exception.UniqueEmailConstraintException;
import icekubit.cloudfilestorage.exception.UniqueNameConstraintException;
import icekubit.cloudfilestorage.minio.MinioService;
import icekubit.cloudfilestorage.model.dto.UserDto;
import icekubit.cloudfilestorage.model.entity.User;
import icekubit.cloudfilestorage.repo.UserRepository;
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
    public void initialize() {
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
                throw new UniqueNameConstraintException();
            } else if (e.getMessage().contains(emailConstraint)) {
                throw new UniqueEmailConstraintException();
            }
        }
        log.info("User was added to database: " + userDto);
        minioService.createFolder("user-" + newUser.getId() + "-files");
    }
}
