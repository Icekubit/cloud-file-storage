package icekubit.cloudfilestorage.service;

import icekubit.cloudfilestorage.model.dto.UserDto;
import icekubit.cloudfilestorage.model.entity.User;
import icekubit.cloudfilestorage.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    @Autowired
    private UserRepository userRepository;

    public void registerNewUser(UserDto userDto) {
        User newUser = new User();
        newUser.setName(userDto.getName());
        newUser.setPassword(userDto.getPassword());
        newUser.setEmail(userDto.getEmail());
        userRepository.save(newUser);
    }
}
