package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.dto.BreadCrumbDto;
import icekubit.cloudfilestorage.minio.MinioService;
import icekubit.cloudfilestorage.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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
            model.addAttribute("path", path);
            model.addAttribute("root", "user-" + userId + "-files");
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

    @PostMapping("/")
    public String handleFileUpload(@RequestParam MultipartFile file,
                                                   @RequestParam String path,
                                                   HttpSession httpSession) {
        System.out.println(file.getOriginalFilename());
        System.out.println(path);
        Integer userId = (Integer) httpSession.getAttribute("userId");
        String minioPathToFile = "";
        if (path.isEmpty()) {
            minioPathToFile = "user-" + userId + "-files/" + file.getOriginalFilename();
        } else {
            minioPathToFile = "user-" + userId + "-files/" + path + "/" + file.getOriginalFilename();
        }
        minioService.uploadMultipartFile(minioPathToFile, file);
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @PostMapping("/create_folder")
    public String createFolder(@RequestParam String folderName,
                               @RequestParam String path,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        String minioPathToFolder = "";
        if (path.isEmpty()) {
            minioPathToFolder = "user-" + userId + "-files/" + folderName + "/";
        } else {
            minioPathToFolder = "user-" + userId + "-files/" + path + "/" + folderName + "/";
        }
        minioService.createFolder(minioPathToFolder);
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @PostMapping("/upload_folder")
    public String handleFolderUpload(@RequestParam MultipartFile[] files,
                                   @RequestParam String path,
                                   HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        String minioPathToFile = "";
        for (MultipartFile file: files) {
            if (path.isEmpty()) {
                minioPathToFile = "user-" + userId + "-files/" + file.getOriginalFilename();
            } else {
                minioPathToFile = "user-" + userId + "-files/" + path + "/" + file.getOriginalFilename();
            }
            minioService.uploadMultipartFile(minioPathToFile, file);
        }
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @PostMapping("/delete_item")
    public String handleItemDeleting(@RequestParam String itemForDeleting,
                                     @RequestParam String path,
                                     HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        minioService.removeObject(itemForDeleting, userId);
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }


}
