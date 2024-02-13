package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.mapper.MinioMapper;
import icekubit.cloudfilestorage.minio.MinioService;
import icekubit.cloudfilestorage.service.CustomUserDetails;
import org.springframework.security.core.Authentication;
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
    public String showHomePage(Authentication authentication,
                               @RequestParam String query,
                               Model model) {
        Integer userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        model.addAttribute("foundItems",
                minioService.searchObjects(userId, query)
                        .stream()
                        .map(minioMapper::convertItemDoDto)
                        .collect(Collectors.toList()));


        return "searching-page";
    }
}
