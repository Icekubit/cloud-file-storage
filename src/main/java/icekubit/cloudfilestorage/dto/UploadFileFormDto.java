package icekubit.cloudfilestorage.dto;

import icekubit.cloudfilestorage.validation.MaxPathLengthConstraint;
import icekubit.cloudfilestorage.validation.UniqueItemNameConstraint;
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
