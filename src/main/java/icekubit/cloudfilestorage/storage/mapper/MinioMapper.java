package icekubit.cloudfilestorage.storage.mapper;

import icekubit.cloudfilestorage.storage.dto.MinioItemDto;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class MinioMapper {
    public MinioItemDto convertItemDoDto(Item item) {
        MinioItemDto minioItemDto = new MinioItemDto();
        minioItemDto.setIsDirectory(item.isDir());

        Path path = Paths.get(item.objectName());
        Path pathToRootFolder = path.getName(0);
        Path relativePathToObject = pathToRootFolder.relativize(path);

        if (item.isDir()) {
            minioItemDto.setRelativePath(relativePathToObject + "/");
        } else {
            minioItemDto.setRelativePath(relativePathToObject.toString());
        }

        Path relativePathToParentFolder = relativePathToObject.getParent();

        if (relativePathToParentFolder == null) {
            minioItemDto.setRelativePathToParentFolder("/");
        } else {
            minioItemDto.setRelativePathToParentFolder(relativePathToParentFolder.toString());
        }

        return minioItemDto;
    }
}
