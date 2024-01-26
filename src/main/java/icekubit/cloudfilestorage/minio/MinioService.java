package icekubit.cloudfilestorage.minio;

import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService {
    private final MinioRepo minioRepo;

    public MinioService(MinioRepo minioRepo) {
        this.minioRepo = minioRepo;
    }


    @SneakyThrows
    public void createFolder(String minioPathToFolder) {
        minioRepo.createFolder(minioPathToFolder);
    }

    @SneakyThrows
    public void uploadMultipartFile(String minioPathToFile, MultipartFile file) {
        minioRepo.uploadFile(file, minioPathToFile);
    }

    @SneakyThrows
    public boolean doesFolderExist(String minioPathToFile) {
        return minioRepo.doesFolderExist(minioPathToFile);
    }

    public List<Item> getListOfItems(String minioPathToFolder) {
        return minioRepo.getListOfItems(minioPathToFolder);
    }

    public void removeObject(String minioPathToObject) {
        minioRepo.removeObject(minioPathToObject);

    }

    public void renameObject(String minioPathToObject, String newObjectName) {
        if (minioPathToObject.endsWith("/")) {
            String newFolder = Paths.get(minioPathToObject).getParent().toString() + "/" + newObjectName + "/";
            createFolder(newFolder);
            minioRepo.copyFolder(minioPathToObject, newFolder);
        } else {
            String minioPathToRenamedObject = Paths.get(minioPathToObject).getParent().toString() + "/" + newObjectName;
            minioRepo.copyFile(minioPathToObject, minioPathToRenamedObject);
        }
        minioRepo.removeObject(minioPathToObject);
    }

    public List<Item> searchObjects(String minioPathToRootFolder, String query) {
        List<Item> foundObjects = new ArrayList<>();
        for (Item item: minioRepo.getListOfItemsRecursively(minioPathToRootFolder)) {
            String fileName = Paths.get(item.objectName()).getFileName().toString();
            if (fileName.toLowerCase().contains(query.toLowerCase())) {
                foundObjects.add(item);
            }
        }
        return foundObjects;
    }
}
