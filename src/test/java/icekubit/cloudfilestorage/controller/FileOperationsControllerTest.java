package icekubit.cloudfilestorage.controller;

import icekubit.cloudfilestorage.storage.controller.FileOperationsController;
import icekubit.cloudfilestorage.storage.service.MinioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FileOperationsControllerTest {

    @Mock
    private MinioService minioService;

    @InjectMocks
    private FileOperationsController fileOperationsController;

    private MockMvc mockMvc;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fileOperationsController).build();
        session = new MockHttpSession();
        session.setAttribute("userId", 42);
    }

    @Test
    public void testRemoveObject() throws Exception {

        mockMvc.perform(delete("/file")
                        .param("objectForDeletion", "testObject")
                        .param("path", "/test/path")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?path=%2Ftest%2Fpath"))
                .andExpect(flash().attributeCount(0));  // Assuming no flash attributes are set

        // Verifying that the removeObject method is called with the correct arguments
        verify(minioService).removeObject(eq(42), eq("testObject"));
    }


    @Test
    void testUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes());


        mockMvc.perform(multipart("/file/upload")
                        .file(file)
                        .param("path", "")
                        .session(session))
                .andExpect(redirectedUrl("/"));

        verify(minioService).uploadMultipartFile(42, "", file);
    }

    @Test
    void testCreateFolder() throws Exception {
        String folderName = "newFolder";
        String path = "path/to/current/folder";

        mockMvc.perform(post("/folder")
                        .param("folderName", folderName)
                        .param("path", path)
                        .session(session))
                .andExpect(redirectedUrl("/?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8)));

        verify(minioService).createFolder(42, path, folderName);
    }

    @Test
    void testUploadFolder() throws Exception {
        String path = "path/to/current/folder";

        MockMultipartFile file1 =
                new MockMultipartFile(
                        "files",
                        "file1.txt",
                        "text/plain",
                        "File 1 Content".getBytes());
        MockMultipartFile file2
                = new MockMultipartFile(
                        "files",
                "file2.txt",
                "text/plain",
                "File 2 Content".getBytes());

        MockHttpServletRequestBuilder requestBuilder
                = MockMvcRequestBuilders.multipart("/folder/upload")
                .file(file1)
                .file(file2)
                .param("path", path)
                .session(session);

        mockMvc.perform(requestBuilder)
                .andExpect(redirectedUrl("/?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8)));

        verify(minioService).uploadMultipartFile(42, path, file1);
        verify(minioService).uploadMultipartFile(42, path, file2);
    }


}
