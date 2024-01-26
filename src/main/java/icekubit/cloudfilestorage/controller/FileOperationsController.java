package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.minio.MinioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class FileOperationsController {
    private final MinioService minioService;

    public FileOperationsController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/file/upload")
    public String uploadFile(@RequestParam MultipartFile file,
                                   @RequestParam String path,
                                   HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        minioService.uploadMultipartFile(userId, path, file);
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @PostMapping("/folder")
    public String createFolder(@RequestParam String folderName,
                               @RequestParam String path,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        minioService.createFolder(userId, path, folderName);
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @PostMapping("/folder/upload")
    public String uploadFolder(@RequestParam MultipartFile[] files,
                                     @RequestParam String path,
                                     HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        for (MultipartFile file: files) {
            minioService.uploadMultipartFile(userId, path, file);
        }
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @DeleteMapping("/file")
    public String removeObject(@RequestParam String objectForDeletion,
                                     @RequestParam String path,
                                     HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        minioService.removeObject(userId, objectForDeletion);

        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @PutMapping("/file")
    public String renameObject(@RequestParam String relativePathToObject,
                               @RequestParam String newObjectName,
                               @RequestParam String path,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        minioService.renameObject(userId, relativePathToObject, newObjectName);
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }
}
