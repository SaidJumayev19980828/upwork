package com.nasnav.test.commons.test_templates;

import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.service.FileService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.nasnav.test.commons.TestCommons.getTempDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:test.database.properties")
public abstract class AbstractTestWithTempBaseDir {
    @Autowired
    protected FileService fileService;

    @Autowired
    private AppConfig appConfig;

    protected Path basePath;

    @Before
    public void setup() throws IOException {
        this.basePath = getTempDirectory();
        ReflectionTestUtils.setField(fileService, "basePath", this.basePath);
        ReflectionTestUtils.setField(appConfig, "basePathStr", this.basePath.toString());

        System.out.println("Test Files Base Path  >>>> " + basePath.toAbsolutePath());

        //The base directory must exists for all tests
        assertTrue(Files.exists(basePath));

        //assert an empty temp directory was created for the test
        try(Stream<Path> files = Files.list(basePath)){
            assertEquals(0L, files.count());
        }
    }
}
