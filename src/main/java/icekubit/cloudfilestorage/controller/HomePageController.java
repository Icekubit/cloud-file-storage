package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.minio.MinioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class HomePageController {
    private final MinioService minioService;

    public HomePageController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping("/")
    public String showHomePage(@RequestParam(required = false) String path,
                               Model model) {
        if (path != null && minioService.isPathValid(path)) {
            model.addAttribute("path", path);
        } else if (path != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "home";
    }


}
