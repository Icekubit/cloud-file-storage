package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.auth.controller.AuthController;
import icekubit.cloudfilestorage.auth.model.CustomUserDetails;
import icekubit.cloudfilestorage.auth.model.dto.UserDto;
import icekubit.cloudfilestorage.auth.service.CustomUserDetailsService;
import icekubit.cloudfilestorage.auth.service.RegistrationService;
import icekubit.cloudfilestorage.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    public void testShowLoginPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void testShowRegistrationForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"));
    }

    @Test
    public void testRegisterAndLogin() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("testUser")
                .email("test@example.com")
                .password("password")
                .build();

        doNothing().when(registrationService).registerNewUser(any(UserDto.class));

        UserDetails userDetails = CustomUserDetails.builder()
                .username(userDto.getName())
                .password(userDto.getPassword())
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        mockMvc.perform(MockMvcRequestBuilders.post("/registration")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", userDto.getName())
                        .param("email", userDto.getEmail())
                        .param("password", userDto.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }
}
