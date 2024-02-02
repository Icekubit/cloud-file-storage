package icekubit.cloudfilestorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RenameObjectForm {
    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[^/]*$", message = "Name cannot contain a slash")
    @Size(max = 240, message = "Object name should be less than 256 characters")
    private String objectName;
}
