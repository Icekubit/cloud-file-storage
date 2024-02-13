package icekubit.cloudfilestorage.storage.dto;

import icekubit.cloudfilestorage.storage.validation.MaxPathLengthConstraint;
import icekubit.cloudfilestorage.storage.validation.UniqueItemNameConstraint;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@UniqueItemNameConstraint
@MaxPathLengthConstraint
public class UploadFileFormDto implements Validatable {
    private MultipartFile file;
    private String currentPath;

    @Override
    public String getObjectName() {
        return file.getOriginalFilename();
    }
}
