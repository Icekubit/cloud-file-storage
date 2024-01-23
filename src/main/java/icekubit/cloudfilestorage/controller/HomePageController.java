package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.dto.MinioItemDto;
import icekubit.cloudfilestorage.minio.MinioService;
import icekubit.cloudfilestorage.repo.UserRepository;
import io.minio.Result;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

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
                               HttpSession httpSession,
                               @RequestParam(required = false) String path,
                               Model model) {
        if ((authentication == null || !authentication.isAuthenticated()) && path != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Integer userId = 0;
        if (authentication != null && authentication.isAuthenticated()) {
            userId = (Integer) httpSession.getAttribute("userId");
            if (userId == null) {
                userId = getUserId(authentication);
                httpSession.setAttribute("userId", userId);
            }
        }

        if (path != null && path.endsWith("/")) {
            return "redirect:/?path=" + path.substring(0, path.length() - 1);
        }

        if ((path != null && minioService.doesFolderExist(path, userId))
        || (authentication != null && authentication.isAuthenticated())) {
            if (path == null) {
                path = "";
            }
            var listOfItems = minioService.getListOfItems(path, userId);
            model.addAttribute("path", "user-" + userId + "-files/" + path);
            model.addAttribute("listOfItems", listOfItems);
        } else if (path != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return "home";
    }


    private int getUserId(Authentication authentication) {
        User userDetails = (User) authentication.getPrincipal();
        String userName = userDetails.getUsername();
        return userRepository.findByName(userName).get().getId();
    }

    private MinioItemDto convertMinioItemToDto(Item item) {
        MinioItemDto result = new MinioItemDto();
        result.setIsDirectory(item.isDir());
        result.setPath(item.objectName());
        String path = item.objectName();
        result.setRelativePath(path.substring(path.indexOf("-files") + 7));
        return result;
    }


}
