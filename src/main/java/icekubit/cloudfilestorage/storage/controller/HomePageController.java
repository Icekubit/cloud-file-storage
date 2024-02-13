package icekubit.cloudfilestorage.storage.controller;

import icekubit.cloudfilestorage.storage.dto.BreadCrumbDto;
import icekubit.cloudfilestorage.storage.dto.CreateFolderFormDto;
import icekubit.cloudfilestorage.storage.dto.RenameFormDto;
import icekubit.cloudfilestorage.storage.mapper.MinioMapper;
import icekubit.cloudfilestorage.storage.service.MinioService;
import icekubit.cloudfilestorage.auth.model.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomePageController {
    private final MinioService minioService;
    private final MinioMapper minioMapper;

    public HomePageController(MinioService minioService,
                              MinioMapper minioMapper) {
        this.minioService = minioService;
        this.minioMapper = minioMapper;
    }

    @GetMapping("/")
    public String showHomePage(Authentication authentication,
                               @RequestParam(required = false) String path,
                               Model model) {
        if ((authentication == null || !authentication.isAuthenticated()) && path != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Integer userId = 0;
        if (authentication != null && authentication.isAuthenticated()) {
            userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
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
            model.addAttribute("folderForm", new CreateFolderFormDto());
            model.addAttribute("renameFormDto", new RenameFormDto());
            model.addAttribute("uploadFileFormDto", new RenameFormDto());
            model.addAttribute("uploadFolderFormDto", new RenameFormDto());
        } else if (path != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return "home";
    }

    private List<BreadCrumbDto> makeBreadCrumbsFromPath(String path) {
        if (path.isEmpty()) {
            return Collections.emptyList();
        }

        List<BreadCrumbDto> breadCrumbs = new ArrayList<>();
        Iterator<Path> iterator = Paths.get(path).iterator();
        Path pathForLink = Paths.get("");
        while (iterator.hasNext()) {
            Path currentObject = iterator.next();
            pathForLink = pathForLink.resolve(currentObject);
            BreadCrumbDto breadCrumbDto = new BreadCrumbDto();
            breadCrumbDto.setDisplayName(currentObject.toString());
            breadCrumbDto.setPathForLink(pathForLink + "/");
            breadCrumbs.add(breadCrumbDto);
        }

        return breadCrumbs;
    }
}
