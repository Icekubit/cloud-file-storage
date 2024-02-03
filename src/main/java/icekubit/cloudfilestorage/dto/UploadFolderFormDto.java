package icekubit.cloudfilestorage.dto;

import icekubit.cloudfilestorage.validation.MaxPathLengthConstraint;
import icekubit.cloudfilestorage.validation.UniqueItemNameConstraint;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;

@Data
@UniqueItemNameConstraint
@MaxPathLengthConstraint
public class UploadFolderFormDto implements Validatable {
    private MultipartFile[] files;
    private String currentPath;

    @Override
    public String getObjectName() {
        return Paths.get(files[0].getOriginalFilename()).getName(0).toString();
    }
}