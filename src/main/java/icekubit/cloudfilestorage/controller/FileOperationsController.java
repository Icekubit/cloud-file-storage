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
import java.nio.file.Paths;

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
        String minioPathToFile = "";
        if (path.isEmpty()) {
            minioPathToFile = getRootFolder(userId) + file.getOriginalFilename();
        } else {
            minioPathToFile = getRootFolder(userId) + path + "/" + file.getOriginalFilename();
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
            minioPathToFolder = getRootFolder(userId) + folderName + "/";
        } else {
            minioPathToFolder = getRootFolder(userId) + path + "/" + folderName + "/";
        }
        minioService.createFolder(minioPathToFolder);
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @PostMapping("/folder/upload")
    public String uploadFolder(@RequestParam MultipartFile[] files,
                                     @RequestParam String path,
                                     HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        String minioPathToFile = "";
        for (MultipartFile file: files) {
            if (path.isEmpty()) {
                minioPathToFile = getRootFolder(userId) + file.getOriginalFilename();
            } else {
                minioPathToFile = getRootFolder(userId) + path + "/" + file.getOriginalFilename();
            }
            minioService.uploadMultipartFile(minioPathToFile, file);
        }
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @DeleteMapping("/file")
    public String removeObject(@RequestParam String objectForDeletion,
                                     @RequestParam String path,
                                     HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        String minioPathToObject = getRootFolder(userId) + objectForDeletion;
        minioService.removeObject(minioPathToObject);

        // check if parent folder for itemForDeletion is empty and create Minio emulation for empty folder
        String minioPathToParentFolder = Paths.get(minioPathToObject).getParent().toString() + "/";
        if (minioService.getListOfItems(minioPathToParentFolder).isEmpty()) {
            minioService.createFolder(minioPathToParentFolder);
        }

        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @PutMapping("/file")
    public String renameObject(@RequestParam String relativePathToObject,
                               @RequestParam String newObjectName,
                               @RequestParam String path,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        String minioPathToObject = getRootFolder(userId) + relativePathToObject;
        minioService.renameObject(minioPathToObject, newObjectName);
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    private String getRootFolder(Integer userId) {
        return "user-" + userId + "-files/";
    }
}
