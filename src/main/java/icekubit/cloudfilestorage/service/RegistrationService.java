package icekubit.cloudfilestorage.service;

import icekubit.cloudfilestorage.exception.UniqueEmailConstraintException;
import icekubit.cloudfilestorage.exception.UniqueNameConstraintException;
import icekubit.cloudfilestorage.model.dto.UserDto;
import icekubit.cloudfilestorage.model.entity.User;
import icekubit.cloudfilestorage.repo.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String nameConstraint;
    private String emailConstraint;

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
    }
}
