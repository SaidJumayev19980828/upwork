package com.nasnav.yeshtery.test;

import com.nasnav.AppConfig;
import com.nasnav.yeshtery.Yeshtery;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
public class FileControllerTest {
    @Autowired
    private AppConfig appConfig;

    Path basePath;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setBasePath() {
        basePath = Paths.get(appConfig.getBasePathStr());
    }

    @After
    public void cleanup() throws IOException {
        clearBaseDirectoryContent();
    }

    private void clearBaseDirectoryContent() throws IOException {
        File file = new File(basePath.toString());

        if(Files.exists(basePath))
            FileUtils.cleanDirectory(file);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/User_Test_Data.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void uploadFileTestWithInvalidMimes() throws Exception {
        List<MockMultipartFile> invalidFiles = getInvalidFiles();

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        for (MockMultipartFile file : invalidFiles) {
            mockMvc.perform(MockMvcRequestBuilders.multipart("/v1/files")
                            .file(file)
                            .header("User-Token", "123")
                            .param("org_id", "99001")
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .andExpect(status().is(406));
        }

    }

    private List<MockMultipartFile> getInvalidFiles() {
        List<MockMultipartFile> files = new ArrayList<>();

        files.add(getDummyPdf());
        files.add(getDummyText());
        files.add(getDummyHtml());

        return files;
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/User_Test_Data.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void uploadFileTestWithValidMimes() throws Exception {
        List<MockMultipartFile> validFiles = getValidFiles();

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        for (MockMultipartFile file : validFiles) {
            mockMvc.perform(MockMvcRequestBuilders.multipart("/v1/files")
                            .file(file)
                            .header("User-Token", "123")
                            .param("org_id", "99001")
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .andExpect(status().is(200));
        }

    }

    private List<MockMultipartFile> getValidFiles() {
        List<MockMultipartFile> files = new ArrayList<>();

        files.add(getDummyImage_png());
        files.add(getDummyImage_jpeg());
        files.add(getDummyVideo());

        return files;
    }

    private MockMultipartFile getDummyPdf() {
        return new MockMultipartFile(
                "file",
                "test_file.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content".getBytes());
    }

    private MockMultipartFile getDummyText() {
        return new MockMultipartFile(
                "file",
                "test_file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "content".getBytes());
    }

    private MockMultipartFile getDummyHtml() {
        return new MockMultipartFile(
                "file",
                "test_file.html",
                MediaType.TEXT_HTML_VALUE,
                "content".getBytes());
    }

    private MockMultipartFile getDummyImage_png() {
        return new MockMultipartFile(
                "file",
                "test_file.png",
                MediaType.IMAGE_PNG_VALUE,
                "content".getBytes());
    }

    private MockMultipartFile getDummyImage_jpeg() {
        return new MockMultipartFile(
                "file",
                "test_file.png",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes());
    }

    private MockMultipartFile getDummyVideo() {
        return new MockMultipartFile(
                "file",
                "test_file.mp4",
                "video/mp4",
                "content".getBytes());
    }
}
