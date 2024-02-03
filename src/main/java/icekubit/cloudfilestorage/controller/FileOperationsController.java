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
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class FileOperationsController {
    private final MinioService minioService;

    public FileOperationsController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/file/upload")
    public RedirectView uploadFile(@Valid UploadFileFormDto uploadFileFormDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");

        String path = uploadFileFormDto.getCurrentPath();
        MultipartFile file = uploadFileFormDto.getFile();

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("uploadFileValidationErrors", bindingResult.getAllErrors());
            return buildRedirectView(path);
        }

        minioService.uploadMultipartFile(userId, path, file);
        return buildRedirectView(path);
    }

    @PostMapping("/folder/upload")
    public RedirectView uploadFolder(@Valid UploadFolderFormDto uploadFolderFormDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");

        MultipartFile[] files = uploadFolderFormDto.getFiles();
        String path = uploadFolderFormDto.getCurrentPath();

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("uploadFolderValidationErrors"
                    , bindingResult.getAllErrors());
            return buildRedirectView(path);
        }

        for (MultipartFile file: files) {
            minioService.uploadMultipartFile(userId, path, file);
        }
        return buildRedirectView(path);
    }

    @PostMapping("/folder")
    public RedirectView createFolder(@Valid CreateFolderFormDto folderForm,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        String path = folderForm.getCurrentPath();

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("createFolderValidationErrors"
                    , bindingResult.getAllErrors());
            return buildRedirectView(path);
        }

        minioService.createFolder(userId, path, folderForm.getObjectName());
        return buildRedirectView(path);
    }



    @DeleteMapping("/file")
    public RedirectView removeObject(@RequestParam String objectForDeletion,
                                     @RequestParam String path,
                                     HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        minioService.removeObject(userId, objectForDeletion);

        return buildRedirectView(path);
    }

    @PutMapping("/file")
    public RedirectView renameObject(@Valid RenameFormDto renameFormDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");

        String relativePathToObject = renameFormDto.getRelativePathToObject();
        String path = renameFormDto.getCurrentPath();

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("renameValidationErrors", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("relativePathToItemWithError", relativePathToObject);
            return buildRedirectView(path);
        }

        minioService.renameObject(userId, relativePathToObject, renameFormDto.getObjectName());
        return buildRedirectView(path);
    }

    private RedirectView buildRedirectView(String path) {
        if (path.isBlank()) {
            return new RedirectView("/");
        }
        return new RedirectView(UriComponentsBuilder.fromPath("/")
                .queryParam("path", URLEncoder.encode(path, StandardCharsets.UTF_8))
                .toUriString());
    }
}
