package icekubit.cloudfilestorage.storage.dto;

import icekubit.cloudfilestorage.storage.validation.MaxPathLengthConstraint;
import icekubit.cloudfilestorage.storage.validation.UniqueItemNameConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@UniqueItemNameConstraint
@MaxPathLengthConstraint
public class CreateFolderFormDto implements FileOperationsDto {
    @NotBlank(message = "Folder name is required")
    @Pattern(regexp = "^(?![\\s\\S]*[/\\\\:*?\"<>|]).*",
            message = "Folder name cannot contain any of these characters: \\, /, :, *, ?, \", <, >, |")
    @Size(max = 240, message = "Folder name should be less than 256 characters")
    private String objectName;
    private String currentPath;
}