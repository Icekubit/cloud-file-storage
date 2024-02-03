package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.dto.CreateFolderFormDto;
import icekubit.cloudfilestorage.dto.RenameFormDto;
import icekubit.cloudfilestorage.dto.UploadFileFormDto;
import icekubit.cloudfilestorage.dto.UploadFolderFormDto;
import icekubit.cloudfilestorage.minio.MinioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class FileOperationsController {
    private final MinioService minioService;

    public FileOperationsController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/file/upload")
    public String uploadFile(@Valid UploadFileFormDto uploadFileFormDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");

        String path = uploadFileFormDto.getCurrentPath();
        MultipartFile file = uploadFileFormDto.getFile();

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("uploadFileValidationErrors", bindingResult.getAllErrors());
            return "redirect:/" +
                    ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
        }

        minioService.uploadMultipartFile(userId, path, file);
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @PostMapping("/folder")
    public String createFolder(@Valid CreateFolderFormDto folderForm,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        String path = folderForm.getCurrentPath();

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("createFolderValidationErrors"
                    , bindingResult.getAllErrors());
            return "redirect:/" +
                    ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
        }

        minioService.createFolder(userId, path, folderForm.getObjectName());
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @PostMapping("/folder/upload")
    public String uploadFolder(@Valid UploadFolderFormDto uploadFolderFormDto,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");

        MultipartFile[] files = uploadFolderFormDto.getFiles();
        String path = uploadFolderFormDto.getCurrentPath();

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("uploadFolderValidationErrors"
                    , bindingResult.getAllErrors());
            return "redirect:/" +
                    ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
        }

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
    public String renameObject(@Valid RenameFormDto renameFormDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");

        String relativePathToObject = renameFormDto.getRelativePathToObject();
        String path = renameFormDto.getCurrentPath();

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("renameValidationErrors", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("relativePathToItemWithError", relativePathToObject);
            return "redirect:/" +
                    ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
        }

        minioService.renameObject(userId, relativePathToObject, renameFormDto.getObjectName());
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }
}
