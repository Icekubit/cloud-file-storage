package icekubit.cloudfilestorage.dto;

import icekubit.cloudfilestorage.validation.MaxPathLengthConstraint;
import icekubit.cloudfilestorage.validation.UniqueItemNameConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@UniqueItemNameConstraint
@MaxPathLengthConstraint
public class CreateFolderFormDto implements Validatable {
    @NotBlank(message = "Folder name is required")
    @Pattern(regexp = "^[^/]*$", message = "Folder name cannot contain a slash")
    @Size(max = 240, message = "Folder name should be less than 256 characters")
    private String objectName;
    private String currentPath;
}