package icekubit.cloudfilestorage.storage.controller;

import icekubit.cloudfilestorage.storage.dto.CreateFolderFormDto;
import icekubit.cloudfilestorage.storage.dto.RenameFormDto;
import icekubit.cloudfilestorage.storage.dto.UploadFileFormDto;
import icekubit.cloudfilestorage.storage.dto.UploadFolderFormDto;
import icekubit.cloudfilestorage.storage.service.MinioService;
import icekubit.cloudfilestorage.auth.model.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public RedirectView uploadFile(@Valid UploadFileFormDto uploadFileFormDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Authentication authentication) {
        Integer userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        String path = uploadFileFormDto.getCurrentPath();
        MultipartFile file = uploadFileFormDto.getFile();

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("uploadFileValidationErrors"
                    , bindingResult.getAllErrors());
            return buildRedirectView(path);
        }

        minioService.uploadMultipartFile(userId, path, file);
        return buildRedirectView(path);
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> downloadFile(@RequestParam String pathToFile,
                             Authentication authentication) {
        Integer userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        ByteArrayResource byteArrayResource = null;
        try (InputStream inputStream = minioService.downloadFile(userId, pathToFile)){
            byteArrayResource = new ByteArrayResource(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        HttpHeaders headers = new HttpHeaders();
        String fileName = Paths.get(pathToFile).getFileName().toString();
        String urlEncodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + urlEncodedFileName + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(byteArrayResource);
    }

    @PostMapping("/folder/upload")
    public RedirectView uploadFolder(@Valid UploadFolderFormDto uploadFolderFormDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        Integer userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

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

    @GetMapping("/folder")
    public ResponseEntity<ByteArrayResource> downloadFolder(@RequestParam String pathToFolder,
                                                            Authentication authentication) {
        Integer userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        String zipArchiveName = Paths.get(pathToFolder).getFileName() + ".zip";

        minioService.saveFolderAsZip(userId, pathToFolder);



        ByteArrayResource byteArrayResource = null;
        try (InputStream inputStream = new FileInputStream(zipArchiveName)){
            byteArrayResource = new ByteArrayResource(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File zipFile = new File(zipArchiveName);
        if (zipFile.exists()) {
            zipFile.delete();
        }


        HttpHeaders headers = new HttpHeaders();
        String urlEncodedZipArchiveName = URLEncoder.encode(zipArchiveName, StandardCharsets.UTF_8);
        headers.add(HttpHeaders.CONTENT_DISPOSITION
                , "attachment; filename=\"" + urlEncodedZipArchiveName + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(byteArrayResource);
    }

    @PostMapping("/folder")
    public RedirectView createFolder(@Valid CreateFolderFormDto folderForm,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Authentication authentication) {
        Integer userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
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
                                     @RequestParam String currentPath,
                                     Authentication authentication) {
        Integer userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        minioService.removeObject(userId, objectForDeletion);
        return buildRedirectView(currentPath);

    }

    @PutMapping("/file")
    public RedirectView renameObject(@Valid RenameFormDto renameFormDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        Integer userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

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
                .queryParam("path", path)
                .toUriString());
    }
}
