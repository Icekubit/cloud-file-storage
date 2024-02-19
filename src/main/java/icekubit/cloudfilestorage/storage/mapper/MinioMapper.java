package icekubit.cloudfilestorage.storage.mapper;

import icekubit.cloudfilestorage.storage.dto.MinioItemDto;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class MinioMapper {
    public MinioItemDto convertItemToDto(Item item) {
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
            String urlEncodedRelativePathToParentFolder
                    = UriUtils.encode(relativePathToParentFolder.toString(), StandardCharsets.UTF_8);
            minioItemDto.setRelativePathToParentFolder(urlEncodedRelativePathToParentFolder);
        }

        minioItemDto.setObjectName(path.getFileName().toString());

        return minioItemDto;
    }
}
