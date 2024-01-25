package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.minio.MinioService;
import icekubit.cloudfilestorage.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchingController {
    private final MinioService minioService;
    private final UserRepository userRepository;

    public SearchingController(MinioService minioService, UserRepository userRepository) {
        this.minioService = minioService;
        this.userRepository = userRepository;
    }

    @GetMapping("/search/")
    public String showHomePage(Authentication authentication,
                               HttpSession httpSession,
                               @RequestParam String query,
                               Model model) {
        Integer userId = 0;
        userId = (Integer) httpSession.getAttribute("userId");
        if (userId == null) {
            userId = getUserId(authentication);
            httpSession.setAttribute("userId", userId);
        }

        System.out.println(userId);
        System.out.println(query);
        minioService.searchObjects("user-" + userId + "-files/", query)
                .forEach(item -> System.out.println(item.getPath()));


        return "searching-page";
    }


    private int getUserId(Authentication authentication) {
        User userDetails = (User) authentication.getPrincipal();
        String userName = userDetails.getUsername();
        return userRepository.findByName(userName).get().getId();
    }
}
