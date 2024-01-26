package icekubit.cloudfilestorage.minio;

import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MinioRepo {
    void createFolder(String path);
    void uploadFile(MultipartFile file, String destination);
    void copyFile(String source, String destination);
    void copyFolder(String source, String destination);
    void removeObject(String path);
    Boolean doesFolderExist(String path);
    List<Item> getListOfItems(String path);
    List<Item> getListOfItemsRecursively(String path);
}
