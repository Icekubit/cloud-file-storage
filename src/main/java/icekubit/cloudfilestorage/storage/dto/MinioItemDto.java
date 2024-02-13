package icekubit.cloudfilestorage.storage.dto;

import lombok.Data;

@Data
public class MinioItemDto {
    Boolean isDirectory;
    String relativePath;
    String relativePathToParentFolder;
}
