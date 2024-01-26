package icekubit.cloudfilestorage.dto;

import lombok.Data;

@Data
public class MinioItemDto {
    Boolean isDirectory;
    String relativePath;
    String relativePathToParentFolder;
}
