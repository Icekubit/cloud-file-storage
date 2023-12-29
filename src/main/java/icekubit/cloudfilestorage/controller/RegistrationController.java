package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.dto.UserDto;
import icekubit.cloudfilestorage.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "registration";
    }

    @PostMapping("/registration")
    public String processRegistration(@ModelAttribute("userDto") UserDto userDto) {
        registrationService.registerNewUser(userDto);
        return "redirect:/login";
    }
}
