package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.dto.BreadCrumbDto;
import icekubit.cloudfilestorage.mapper.MinioMapper;
import icekubit.cloudfilestorage.minio.MinioService;
import icekubit.cloudfilestorage.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomePageController {
    private final MinioService minioService;
    private final UserRepository userRepository;
    private final MinioMapper minioMapper;

    public HomePageController(MinioService minioService,
                              UserRepository userRepository,
                              MinioMapper minioMapper) {
        this.minioService = minioService;
        this.userRepository = userRepository;
        this.minioMapper = minioMapper;
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


//        if ((path != null && minioService.doesFolderExist("user-" + userId + "-files/" + path + "/"))
        if ((path != null && minioService.doesFolderExist(userId, path))
        && (authentication != null && authentication.isAuthenticated())
        || (path == null && authentication != null && authentication.isAuthenticated())) {
            if (path == null) {
                path = "";
            }
            var listOfItems = minioService.getListOfItems(userId, path)
                    .stream()
                    .map(minioMapper::convertItemDoDto)
                    .collect(Collectors.toList());
            model.addAttribute("path", path);
            model.addAttribute("listOfItems", listOfItems);
            model.addAttribute("breadCrumbs", makeBreadCrumbsFromPath(path));
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

    private List<BreadCrumbDto> makeBreadCrumbsFromPath(String path) {
        if (path.isEmpty()) {
            return Collections.emptyList();
        }

        List<BreadCrumbDto> breadCrumbs = new ArrayList<>();
        String[] breadCrumbsNames = path.split("/");
        String pathForLink = "";
        for (String breadCrumbName: breadCrumbsNames) {
            BreadCrumbDto breadCrumbDto = new BreadCrumbDto();
            breadCrumbDto.setDisplayName(breadCrumbName);
            pathForLink = pathForLink + breadCrumbName + "/";
            breadCrumbDto.setPathForLink(pathForLink);
            breadCrumbs.add(breadCrumbDto);
        }

        return breadCrumbs;
    }
}
