package icekubit.cloudfilestorage.auth.service;


import icekubit.cloudfilestorage.auth.model.CustomUserDetails;
import icekubit.cloudfilestorage.auth.model.entity.User;
import icekubit.cloudfilestorage.auth.repo.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("User " + name + " not found"));
        return CustomUserDetails.builder()
                .username(user.getName())
                .password(user.getPassword())
                .userId(user.getId())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("USER")))
                .build();
    }
}
