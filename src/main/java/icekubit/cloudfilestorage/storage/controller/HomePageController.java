package icekubit.cloudfilestorage.storage.controller;

import icekubit.cloudfilestorage.storage.dto.BreadCrumbDto;
import icekubit.cloudfilestorage.storage.dto.CreateFolderFormDto;
import icekubit.cloudfilestorage.storage.dto.MinioItemDto;
import icekubit.cloudfilestorage.storage.dto.RenameFormDto;
import icekubit.cloudfilestorage.storage.exception.ResourceDoesNotExistException;
import icekubit.cloudfilestorage.storage.mapper.MinioMapper;
import icekubit.cloudfilestorage.storage.service.MinioService;
import icekubit.cloudfilestorage.auth.model.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
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
    public String showHomePage(@RequestParam(required = false) String path,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        Integer userId = userDetails.getUserId();

        if (path == null) {
            path = "";
        }


        if (!path.isEmpty() && !minioService.doesObjectExist(userId, path)) {
            throw new ResourceDoesNotExistException("The folder on the path " + path
                    + " doesn't exist for user " + userDetails.getUsername());
        }


        List<MinioItemDto> listOfItems = minioService.getListOfItems(userId, path)
                .stream()
                .map(minioMapper::convertItemToDto)
                .collect(Collectors.toList());
        model.addAttribute("path", path);
        model.addAttribute("listOfItems", listOfItems);
        model.addAttribute("breadCrumbs", makeBreadCrumbsFromPath(path));
        model.addAttribute("folderForm", new CreateFolderFormDto());
        model.addAttribute("renameFormDto", new RenameFormDto());
        model.addAttribute("uploadFileFormDto", new RenameFormDto());
        model.addAttribute("uploadFolderFormDto", new RenameFormDto());

        return "home";
    }

    private List<BreadCrumbDto> makeBreadCrumbsFromPath(String path) {
        if (path.isEmpty()) {
            return Collections.emptyList();
        }

        String[] objectNames = path.split("/");
        List<BreadCrumbDto> breadCrumbs = new ArrayList<>();
        BreadCrumbDto firstBreadCrumbDto = new BreadCrumbDto();
        firstBreadCrumbDto.setDisplayName(objectNames[0]);
        String pathForLink = objectNames[0];
        firstBreadCrumbDto.setPathForLink(pathForLink);
        breadCrumbs.add(firstBreadCrumbDto);

        for (int i = 1; i < objectNames.length; i++) {
            BreadCrumbDto breadCrumbDto = new BreadCrumbDto();
            breadCrumbDto.setDisplayName(objectNames[i]);
            pathForLink = pathForLink + "%2F" + objectNames[i];
            breadCrumbDto.setPathForLink(pathForLink);
            breadCrumbs.add(breadCrumbDto);
        }


        return breadCrumbs;
    }
}
