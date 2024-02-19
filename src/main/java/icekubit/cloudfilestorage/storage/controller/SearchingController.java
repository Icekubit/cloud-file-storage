package icekubit.cloudfilestorage.storage.controller;

import icekubit.cloudfilestorage.storage.mapper.MinioMapper;
import icekubit.cloudfilestorage.storage.service.MinioService;
import icekubit.cloudfilestorage.auth.model.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.stream.Collectors;

@Controller
public class SearchingController {
    private final MinioService minioService;
    private final MinioMapper minioMapper;

    public SearchingController(MinioService minioService, MinioMapper minioMapper) {
        this.minioService = minioService;
        this.minioMapper = minioMapper;
    }

    @GetMapping("/search/")
    public String showHomePage(@RequestParam String query,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails.getUserId();

        model.addAttribute("queryParam", query);
        model.addAttribute("foundItems",
                minioService.searchObjects(userId, query)
                        .stream()
                        .map(minioMapper::convertItemToDto)
                        .collect(Collectors.toList()));


        return "searching-page";
    }
}
