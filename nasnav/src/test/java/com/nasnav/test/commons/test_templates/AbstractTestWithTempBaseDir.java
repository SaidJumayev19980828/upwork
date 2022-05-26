package com.nasnav.test.commons.test_templates;

import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.service.FileService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:test.database.properties")
@TestPropertySource("/test.application.properties")
public abstract class AbstractTestWithTempBaseDir {
    @Autowired
    protected FileService fileService;

    @Autowired
    private AppConfig appConfig;

    protected Path basePath;

    @Before
    public void setup() throws IOException {
        this.basePath = Paths.get(appConfig.getBasePathStr());

        System.out.println("Test Files Base Path  >>>> " + basePath.toAbsolutePath());

        assertTrue(Files.exists(basePath));
    }

    @After
    public void cleanup() throws IOException {
        clearBaseDirectoryContent();
        assertBaseDirectoryHasNoContent();
    }

    private void clearBaseDirectoryContent() throws IOException {
        File file = new File(basePath.toString());

        if(Files.exists(basePath))
            FileUtils.cleanDirectory(file);
    }

    private void assertBaseDirectoryHasNoContent(){
        try{
            Stream<Path> files = Files.list(basePath);
            assertEquals(0L, files.count());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
