package icekubit.cloudfilestorage.minio;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class MinioController {
    private final MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping("/upload")
    public String showUploadPage(@RequestParam String path, Model model) {
        model.addAttribute("path", path);
        return "upload";
    }


    @PostMapping("/upload")
    public void uploadFile(@RequestParam String path, @RequestPart MultipartFile file) {
        minioService.uploadMultipartFile(path, file);
    }

}
