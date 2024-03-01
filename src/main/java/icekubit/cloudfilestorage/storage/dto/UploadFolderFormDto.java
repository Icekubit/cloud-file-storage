package icekubit.cloudfilestorage.storage.dto;

import icekubit.cloudfilestorage.storage.validation.MaxPathLengthConstraint;
import icekubit.cloudfilestorage.storage.validation.UniqueItemNameConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@UniqueItemNameConstraint
@MaxPathLengthConstraint
public class UploadFolderFormDto implements FileOperationsDto {
    private MultipartFile[] files;
    private String currentPath;
    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^(?![\\s\\S]*[/\\\\:*?\"<>|]).*",
            message = "Object name cannot contain any of these characters: \\, /, :, *, ?, \", <, >, |")
    @Size(max = 240, message = "Object name should be less than 256 characters")
    private String objectName;
}