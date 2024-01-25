package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.dto.FoundItemDto;
import icekubit.cloudfilestorage.dto.MinioItemDto;
import icekubit.cloudfilestorage.minio.MinioService;
import icekubit.cloudfilestorage.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

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

        model.addAttribute("foundItems",
                minioService.searchObjects(getRootFolder(userId), query)
                        .stream().map(this::convertToFoundItemDto).collect(Collectors.toList()));


        return "searching-page";
    }


    private int getUserId(Authentication authentication) {
        User userDetails = (User) authentication.getPrincipal();
        String userName = userDetails.getUsername();
        return userRepository.findByName(userName).get().getId();
    }

    private String getRootFolder(Integer userId) {
        return "user-" + userId + "-files/";
    }

    private FoundItemDto convertToFoundItemDto(MinioItemDto minioItemDto) {
        FoundItemDto foundItemDto = new FoundItemDto();
        foundItemDto.setIsDirectory(minioItemDto.getIsDirectory());
        foundItemDto.setPath(minioItemDto.getPath());
        foundItemDto.setRelativePath(minioItemDto.getRelativePath());

        String pathToParentFolder = Paths.get(minioItemDto.getPath()).getParent().toString() + "/";
        String relativePathToParentFolder
                = pathToParentFolder.substring(pathToParentFolder.indexOf("-files") + 7);

        foundItemDto.setRelativePathToParentFolder(relativePathToParentFolder);
        return foundItemDto;
    }
}
