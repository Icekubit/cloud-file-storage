package icekubit.cloudfilestorage.storage.controller;

import icekubit.cloudfilestorage.storage.dto.CreateFolderFormDto;
import icekubit.cloudfilestorage.storage.dto.RenameFormDto;
import icekubit.cloudfilestorage.storage.dto.UploadFileFormDto;
import icekubit.cloudfilestorage.storage.dto.UploadFolderFormDto;
import icekubit.cloudfilestorage.storage.exception.ResourceDoesNotExistException;
import icekubit.cloudfilestorage.storage.service.MinioService;
import icekubit.cloudfilestorage.auth.model.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Controller
@Slf4j
public class FileOperationsController {
    private final MinioService minioService;

    public FileOperationsController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping("/file")
    public void downloadFile(@RequestParam String pathToFile,
                             HttpServletResponse httpServletResponse,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails.getUserId();

        if (pathToFile.endsWith("/") || !minioService.doesObjectExist(userId, pathToFile)) {
            throw new ResourceDoesNotExistException("Failed to download the file on the path " + pathToFile +
                    " because this file doesn't exist");
        }
        String fileName = Paths.get(pathToFile).getFileName().toString();
        String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);
        httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"");

        try (OutputStream outputStream = httpServletResponse.getOutputStream();
             InputStream inputStream = minioService.downloadFile(userId, pathToFile)) {
            outputStream.write(inputStream.readAllBytes());
            log.info("The file " + pathToFile + " was downloaded by user " + userDetails.getUsername());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/folder")
    public void downloadFolder(@RequestParam String pathToFolder,
                               HttpServletResponse httpServletResponse,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails.getUserId();

        if (!pathToFolder.endsWith("/") || !minioService.doesObjectExist(userId, pathToFolder)) {
            throw new ResourceDoesNotExistException("Failed to download the folder on the path " + pathToFolder +
                    " because this folder doesn't exist");
        }

        String zipArchiveName = Paths.get(pathToFolder).getFileName() + ".zip";

        String encodedZipArchiveName = UriUtils.encode(zipArchiveName, StandardCharsets.UTF_8);
        httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encodedZipArchiveName + "\"");

        try {
            minioService.downloadFolderAsZip(userId, pathToFolder, httpServletResponse.getOutputStream());
            log.info("The folder " + pathToFolder + " was downloaded by user " + userDetails.getUsername());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @PostMapping("/file/upload")
    public RedirectView uploadFile(@Valid UploadFileFormDto uploadFileFormDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails.getUserId();

        String path = uploadFileFormDto.getCurrentPath();
        MultipartFile file = uploadFileFormDto.getFile();

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("uploadFileValidationErrors"
                    , bindingResult.getAllErrors());
            return buildRedirectView(path);
        }

        minioService.uploadMultipartFile(userId, path, file);
        log.info("The file " + file.getOriginalFilename()
                + " was uploaded to the path \"" + path + "\" by user " + userDetails.getUsername());
        return buildRedirectView(path);
    }

    @PostMapping("/folder/upload")
    public RedirectView uploadFolder(@Valid UploadFolderFormDto uploadFolderFormDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails.getUserId();

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
        log.info("The folder " + files[0].getOriginalFilename()
                + " was uploaded to the path \"" + path + "\" by user " + userDetails.getUsername());
        return buildRedirectView(path);
    }



    @PostMapping("/folder")
    public RedirectView createFolder(@Valid CreateFolderFormDto folderForm,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails.getUserId();
        String path = folderForm.getCurrentPath();
        String folderName = folderForm.getObjectName();

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("createFolderValidationErrors"
                    , bindingResult.getAllErrors());
            return buildRedirectView(path);
        }

        minioService.createFolder(userId, path, folderName);
        log.info("The folder " + folderName + " was created on the path \""
                + path + "\" by user " + userDetails.getUsername());
        return buildRedirectView(path);
    }



    @DeleteMapping("/file")
    public RedirectView removeObject(@RequestParam String objectName,
                                     @RequestParam String currentPath,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails.getUserId();
        String pathToObject = currentPath.isBlank() ? objectName : currentPath + "/" + objectName;
        minioService.removeObject(userId, pathToObject);
        log.info("The object " + pathToObject + " was deleted by user " + userDetails.getUsername());
        return buildRedirectView(currentPath);

    }

    @PutMapping("/file")
    public RedirectView renameObject(@Valid RenameFormDto renameFormDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails.getUserId();

        String relativePathToObject = renameFormDto.getRelativePathToObject();
        String path = renameFormDto.getCurrentPath();
        String newObjectName = renameFormDto.getObjectName();

        if (!minioService.doesObjectExist(userId, relativePathToObject)) {
            throw new ResourceDoesNotExistException(
                    "Fail to rename the object on the path \"" + relativePathToObject +
                            "\" because this resource doesn't exist");
        }

        String oldObjectName = Paths.get(relativePathToObject).getFileName().toString();
        if (newObjectName.equals(oldObjectName)) {
            return buildRedirectView(path);
        }



        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("renameValidationErrors", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("relativePathToItemWithError", relativePathToObject);
            return buildRedirectView(path);
        }

        minioService.renameObject(userId, relativePathToObject, newObjectName);
        log.info("The file " + relativePathToObject + " was renamed to \"" + newObjectName + "\"");
        return buildRedirectView(path);
    }

    private RedirectView buildRedirectView(String path) {
        if (path.isBlank()) {
            return new RedirectView(ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/").toUriString());
        }
        return new RedirectView(ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/")
                .queryParam("path", path)
                .toUriString());
    }
}
