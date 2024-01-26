package icekubit.cloudfilestorage.minio;

import icekubit.cloudfilestorage.dto.MinioItemDto;
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

    public List<MinioItemDto> getListOfItems(String minioPathToFolder) {
        List<MinioItemDto> listOfItems = new ArrayList<>();
        for (Item item: minioRepo.getListOfItems(minioPathToFolder)) {
            listOfItems.add(convertMinioItemToDto(item));
        }

        return listOfItems;
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

    public List<MinioItemDto> searchObjects(String minioPathToRootFolder, String query) {
        List<MinioItemDto> allUserItems = new ArrayList<>();
        for (Item item: minioRepo.getListOfItemsRecursively(minioPathToRootFolder)) {
            allUserItems.add(convertMinioItemToDto(item));
        }

        List<MinioItemDto> foundObjects = new ArrayList<>();
        for (MinioItemDto item: allUserItems) {
            String fileName = Paths.get(item.getPath()).getFileName().toString();
            if (fileName.toLowerCase().contains(query.toLowerCase())) {
                foundObjects.add(item);
            }
        }
        return foundObjects;
    }

    private MinioItemDto convertMinioItemToDto(Item item) {
        MinioItemDto result = new MinioItemDto();
        result.setIsDirectory(item.isDir());
        result.setPath(item.objectName());
        String path = item.objectName();
        result.setRelativePath(path.substring(path.indexOf("-files") + 7));
        return result;
    }
}
