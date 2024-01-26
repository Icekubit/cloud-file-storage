package icekubit.cloudfilestorage.mapper;

import icekubit.cloudfilestorage.dto.MinioItemDto;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class MinioMapper {
    public MinioItemDto convertItemDoDto(Item item) {
        MinioItemDto minioItemDto = new MinioItemDto();
        minioItemDto.setIsDirectory(item.isDir());
        String path = item.objectName();
        minioItemDto.setRelativePath(path.substring(path.indexOf("-files") + 7));

        String pathToParentFolder = Paths.get(path).getParent().toString() + "/";
        String relativePathToParentFolder
                = pathToParentFolder.substring(pathToParentFolder.indexOf("-files") + 7);

        minioItemDto.setRelativePathToParentFolder(relativePathToParentFolder);
        return minioItemDto;
    }
}
