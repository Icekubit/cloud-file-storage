package icekubit.cloudfilestorage.auth.model.dto;

import icekubit.cloudfilestorage.auth.validation.UniqueEmailConstraint;
import icekubit.cloudfilestorage.auth.validation.UniqueNameConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString(exclude = "password")
public class UserDto {
    @Size(min = 3, max = 20, message = "Name must be between {min} and {max} characters")
    private String name;
    @Email(message = "Invalid email")
    @NotBlank(message = "This field is required. Please provide an email")
    private String email;
    @Size(min = 5, max = 50, message = "Password must be between {min} and {max} characters")
    private String password;
}
