package icekubit.cloudfilestorage.storage.repo;

import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface MinioRepo {
    void createFolder(String path);
    void uploadFile(InputStream inputStream, String destination);
    InputStream downloadFile(String path);
    void copyFile(String source, String destination);
    void copyFolder(String source, String destination);
    void removeObject(String path);
    void renameObject(String path, String newObjectName);
    Boolean doesObjectExist(String path);
    List<Item> getListOfItems(String path);
    List<Item> getListOfItemsRecursively(String path);
}
