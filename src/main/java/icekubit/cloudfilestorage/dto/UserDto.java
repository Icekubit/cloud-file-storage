package icekubit.cloudfilestorage.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserDto {
    private String name;
    private String email;
    private String password;
}
