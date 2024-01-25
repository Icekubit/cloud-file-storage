package icekubit.cloudfilestorage.dto;

import lombok.Data;

@Data
public class FoundItemDto {
    Boolean isDirectory;
    String path;
    String relativePath;
    String relativePathToParentFolder;
}
