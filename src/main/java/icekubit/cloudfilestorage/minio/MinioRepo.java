package icekubit.cloudfilestorage.minio;

import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MinioRepo {
    public void createFolder(String path);
    public void uploadFile(MultipartFile file, String destination);
    public void copyFile(String source, String destination);
    public void copyFolder(String source, String destination);
    public void removeObject(String path);
    public Boolean doesFolderExist(String path);
    public List<Item> getListOfItems(String path);
    public List<Item> getListOfItemsRecursively(String path);
}
