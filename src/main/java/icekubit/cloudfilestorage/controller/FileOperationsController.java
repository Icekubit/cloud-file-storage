package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.dto.FolderForm;
import icekubit.cloudfilestorage.dto.RenameObjectForm;
import icekubit.cloudfilestorage.minio.MinioService;
import io.minio.messages.Item;
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
import java.nio.file.Path;
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
                                   RedirectAttributes redirectAttributes,
                                   HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");

        if (isRepeatableObjectName(userId, path, file.getOriginalFilename())) {
            redirectAttributes.addFlashAttribute("theSameNameOfUploadingFileError", file.getOriginalFilename());
            return "redirect:/" +
                    ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
        }

        minioService.uploadMultipartFile(userId, path, file);
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @PostMapping("/folder")
    public String createFolder(@Valid FolderForm folderForm,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               @RequestParam String path,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
        if (!bindingResult.hasErrors() && isRepeatableObjectName(userId, path, folderForm.getFolderName())) {
            bindingResult.rejectValue("folderName",
                    "repeatableFolderName",
                    "The folder or the file with this name already exists");
        }

        if (!bindingResult.hasErrors()) {
            if (path.length() + folderForm.getFolderName().length() > 1000) {
                bindingResult.rejectValue("folderName",
                        "tooLongPath",
                        "The path is too long");
            }
        }

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/" +
                    ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
        }

        minioService.createFolder(userId, path, folderForm.getFolderName());
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    private boolean isRepeatableObjectName(Integer userId, String currentPath, String newObjectName) {
        return minioService.getListOfItems(userId, currentPath).stream()
                .map(Item::objectName)
                .map(Paths::get)
                .map(Path::getFileName)
                .map(Path::toString)
                .anyMatch(itemName -> itemName.equals(newObjectName));
    }

    @PostMapping("/folder/upload")
    public String uploadFolder(@RequestParam MultipartFile[] files,
                                     @RequestParam String path,
                                     RedirectAttributes redirectAttributes,
                                     HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");

        String folderName = Paths.get(files[0].getOriginalFilename()).getName(0).toString();

        if (isRepeatableObjectName(userId, path, folderName)) {
            redirectAttributes.addFlashAttribute("theSameNameOfUploadingFolderError", folderName);
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
    public String renameObject(@RequestParam String relativePathToObject,
                               @Valid RenameObjectForm renameObjectForm,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               @RequestParam String path,
                               HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");

        if (!bindingResult.hasErrors() && isRepeatableObjectName(userId, path, renameObjectForm.getObjectName())) {
            bindingResult.rejectValue("objectName",
                    "repeatableObjectName",
                    "The folder or the file with this name already exists");
        }

        if (!bindingResult.hasErrors()) {
            if (path.length() + renameObjectForm.getObjectName().length() > 1000) {
                bindingResult.rejectValue("objectName",
                        "tooLongPath",
                        "The path is too long");
            }
        }

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("relativePathToItemWithError", relativePathToObject);
            return "redirect:/" +
                    ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
        }

        minioService.renameObject(userId, relativePathToObject, renameObjectForm.getObjectName());
        return "redirect:/" +
                ((path.isEmpty()) ? "" : "?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
    }
}
