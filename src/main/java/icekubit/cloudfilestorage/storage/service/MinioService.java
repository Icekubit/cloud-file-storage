package icekubit.cloudfilestorage.storage.service;

import icekubit.cloudfilestorage.storage.repo.MinioRepo;
import io.minio.messages.Item;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService {
    private final MinioRepo minioRepo;

    public MinioService(MinioRepo minioRepo) {
        this.minioRepo = minioRepo;
    }


    public void createRootFolder(Integer userId) {
        String minioPathToFolder = getMinioPathToObject(userId, "") + "/";
        minioRepo.createFolder(minioPathToFolder);
    }

    public void createFolder(Integer userId, String path, String newFolderName) {
        String minioPathToFolder = getMinioPathToObject(userId, path) + "/" + newFolderName + "/";
        minioRepo.createFolder(minioPathToFolder);
    }

    public void uploadMultipartFile(Integer userId, String path, MultipartFile file) {
        String minioPathToFile = getMinioPathToObject(userId, path) + "/" + file.getOriginalFilename();
        minioRepo.uploadFile(file, minioPathToFile);
    }

    public InputStream downloadFile(Integer userId, String path) {
        String minioPathToFile = getMinioPathToObject(userId, path);
        return minioRepo.downloadFile(minioPathToFile);
    }

    public void downloadFolderAsZip(Integer userId, String path, OutputStream outputStream) {
        String minioPathToFolder = getMinioPathToObject(userId, path);

        try (ZipArchiveOutputStream zos
                     = new ZipArchiveOutputStream(outputStream)) {
            zos.setEncoding("Cp437");
            zos.setFallbackToUTF8(true);
            zos.setUseLanguageEncodingFlag(true);
            zos.setCreateUnicodeExtraFields(
                    ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE);

            List<Item> items = minioRepo.getListOfItemsRecursively(minioPathToFolder);
            for (Item item : items) {
                if (!item.isDir()) {
                    Path minioPathToItem = Paths.get(item.objectName());
                    String relativePathToItem = Paths.get(minioPathToFolder).relativize(minioPathToItem).toString();
                    ZipArchiveEntry entry = new ZipArchiveEntry(relativePathToItem);
                    zos.putArchiveEntry(entry);

                    try (InputStream inputStream = minioRepo.downloadFile(item.objectName())) {
                        IOUtils.copy(inputStream, zos);
                    }
                    zos.closeArchiveEntry();
                }
            }
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public boolean doesFolderExist(Integer userId, String path) {
        String minioPathToFolder = getMinioPathToObject(userId, path) + "/";
        return minioRepo.doesFolderExist(minioPathToFolder);
    }

    public boolean doesFileExist(Integer userId, String path) {
        String minioPathToFile = getMinioPathToObject(userId, path);
        return minioRepo.doesFileExist(minioPathToFile);
    }

    public List<Item> getListOfItems(Integer userId, String path) {
        String minioPathToFolder = getMinioPathToObject(userId, path) + "/";
        return minioRepo.getListOfItems(minioPathToFolder);
    }

    public void removeObject(Integer userId, String path) {
        String minioPathToObject = getMinioPathToObject(userId, path);
        minioRepo.removeObject(minioPathToObject);

        // check if parent folder for itemForDeletion is empty and create Minio emulation for empty folder
        String minioPathToParentFolder = Paths.get(minioPathToObject).getParent().toString() + "/";
        if (minioRepo.getListOfItems(minioPathToParentFolder).isEmpty()) {
            minioRepo.createFolder(minioPathToParentFolder);
        }

    }

    public void renameObject(Integer userId, String path, String newObjectName) {
        String minioPathToObject = getMinioPathToObject(userId, path);
        minioRepo.renameObject(minioPathToObject, newObjectName);
    }

    public List<Item> searchObjects(Integer userId, String query) {
        List<Item> foundObjects = new ArrayList<>();
        String minioPathToRootFolder = getMinioPathToObject(userId, "") + "/";
        for (Item item: minioRepo.getListOfItemsRecursively(minioPathToRootFolder)) {
            String fileName = Paths.get(item.objectName()).getFileName().toString();
            if (fileName.toLowerCase().contains(query.toLowerCase())) {
                foundObjects.add(item);
            }
        }
        return foundObjects;
    }

    private String getMinioPathToObject(Integer userId, String path) {
        return "user-" + userId + "-files" + (path.isEmpty() ? "" : ("/" + path));
    }
}
