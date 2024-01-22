package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.minio.MinioService;
import icekubit.cloudfilestorage.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class HomePageController {
    private final MinioService minioService;
    private final UserRepository userRepository;

    public HomePageController(MinioService minioService, UserRepository userRepository) {
        this.minioService = minioService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String showHomePage(Authentication authentication,
                               @RequestParam(required = false) String path,
                               Model model) {
        int userId = 0;
        if (authentication != null && authentication.isAuthenticated()) {
            userId = getUserId(authentication);
        }
        if (path != null && minioService.isPathValid(path, userId)) {
            model.addAttribute("path", path);
        } else if (path != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            model.addAttribute("path", "/");
        }
        return "home";
    }

    private int getUserId(Authentication authentication) {
        User userDetails = (User) authentication.getPrincipal();
        String userName = userDetails.getUsername();
        return userRepository.findByName(userName).get().getId();
    }


}
