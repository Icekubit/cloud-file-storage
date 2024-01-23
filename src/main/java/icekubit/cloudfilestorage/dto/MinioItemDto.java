package icekubit.cloudfilestorage.dto;

import lombok.Data;

@Data
public class MinioItemDto {
    Boolean isDirectory;
    String path;
    String relativePath;
}
