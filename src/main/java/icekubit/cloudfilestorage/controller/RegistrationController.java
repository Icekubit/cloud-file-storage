package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.exception.UniqueEmailConstraintException;
import icekubit.cloudfilestorage.exception.UniqueNameConstraintException;
import icekubit.cloudfilestorage.model.dto.UserDto;
import icekubit.cloudfilestorage.service.CustomUserDetailsService;
import icekubit.cloudfilestorage.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @GetMapping("/registration")
    public String showRegistrationForm(UserDto userDto) {
        return "registration";
    }

    @PostMapping("/registration")
    public String registerAndLogin(HttpServletRequest request, HttpServletResponse response,
                                   @Valid UserDto userDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        try {
            registrationService.registerNewUser(userDto);
        } catch (UniqueNameConstraintException e) {
            bindingResult.rejectValue("name", "name", "The user with this name already exists");
            return "registration";
        } catch (UniqueEmailConstraintException e) {
            bindingResult.rejectValue("email", "email", "The user with this email already exists");
            return "registration";
        }

        loginAfterRegistration(request, response, userDto);
        return "redirect:/hello";
    }

    private void loginAfterRegistration(HttpServletRequest request, HttpServletResponse response, UserDto userDto) {
        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
                .getContextHolderStrategy();
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        var userDetails = userDetailsService.loadUserByUsername(userDto.getName());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
