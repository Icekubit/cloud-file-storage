package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.minio.MinioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    public String handleFileUpload(@RequestParam MultipartFile file,
                                   @RequestParam String path,
                                   HttpSession httpSession) {
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

    @PostMapping("/folder")
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

    @PostMapping("/folder/upload")
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

    @DeleteMapping("/file")
    public String handleItemDeleting(@RequestParam String itemForDeleting,
                                     @RequestParam String path,
                                     HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        minioService.removeObject(itemForDeleting, userId);
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }
}
