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
public class RenameFormDto implements Validatable {
    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[^/]*$", message = "Name cannot contain a slash")
    @Size(max = 240, message = "Object name should be less than 256 characters")
    private String objectName;
    private String relativePathToObject;
    private String currentPath;
}
