package icekubit.cloudfilestorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FolderForm {
    @NotBlank(message = "Folder name is required")
    @Pattern(regexp = "^[^/]*$", message = "Folder name cannot contain a slash")
    @Size(max = 240, message = "Folder name should less than 256 characters")
    private String folderName;
}