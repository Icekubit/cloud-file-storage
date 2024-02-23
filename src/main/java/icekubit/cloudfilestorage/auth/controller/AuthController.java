package icekubit.cloudfilestorage.auth.controller;

import icekubit.cloudfilestorage.auth.exception.UniqueEmailConstraintException;
import icekubit.cloudfilestorage.auth.exception.UniqueNameConstraintException;
import icekubit.cloudfilestorage.auth.model.dto.UserDto;
import icekubit.cloudfilestorage.auth.service.CustomUserDetailsService;
import icekubit.cloudfilestorage.auth.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthController {

    private final RegistrationService registrationService;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(RegistrationService registrationService,
                          CustomUserDetailsService userDetailsService,
                          SecurityContextRepository securityContextRepository) {
        this.registrationService = registrationService;
        this.userDetailsService = userDetailsService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/registration")
    public String showRegistrationForm(UserDto userDto) {
        return "registration";
    }

    @PostMapping("/registration")
    public String registerAndLogin(HttpServletRequest request, HttpServletResponse response,
                                   @Valid UserDto userDto, BindingResult bindingResult) {

        try {
            registrationService.registerNewUser(userDto);
        } catch (UniqueNameConstraintException e) {
            bindingResult.rejectValue("name", "UniqueNameConstraint",
                    "The user with this name already exists");
            log.info("Failed to register user with name {} because the user with this name already exists"
                    , userDto.getName());
            return "registration";
        } catch (UniqueEmailConstraintException e) {
            bindingResult.rejectValue("email", "UniqueEmailConstraint",
                    "The user with this email already exists");
            log.info("Failed to register user with email {} because the user with this email already exists"
                    , userDto.getEmail());
            return "registration";
        }
        log.info("The user " + userDto + " was registered");
        loginAfterRegistration(request, response, userDto);
        return "redirect:/";
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
