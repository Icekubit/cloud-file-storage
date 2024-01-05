package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.exception.UniqueEmailConstraintException;
import icekubit.cloudfilestorage.exception.UniqueNameConstraintException;
import icekubit.cloudfilestorage.model.dto.UserDto;
import icekubit.cloudfilestorage.service.CustomUserDetailsService;
import icekubit.cloudfilestorage.service.RegistrationService;
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
public class RegistrationController {

    private final RegistrationService registrationService;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository;

    public RegistrationController(RegistrationService registrationService,
                                  CustomUserDetailsService userDetailsService,
                                  SecurityContextRepository securityContextRepository) {
        this.registrationService = registrationService;
        this.userDetailsService = userDetailsService;
        this.securityContextRepository = securityContextRepository;
    }

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
            bindingResult.rejectValue("name", "UniqueNameConstraint",
                    "The user with this name already exists");
            log.info("Exception: " + e);
            return "registration";
        } catch (UniqueEmailConstraintException e) {
            bindingResult.rejectValue("email", "UniqueEmailConstraint",
                    "The user with this email already exists");
            log.info("Exception: " + e);
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
