package com.nasnav.test.commons.test_templates;

import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.service.AdminService;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG)
@PropertySource("classpath:test.database.properties")
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = BaseTestConfiguration.class)
public abstract class AbstractTestWithTempBaseDir {
    @Autowired
    private AdminService adminService;

    @Autowired
    private AppConfig appConfig;

    protected Path basePath;

    @After
    @AfterEach
    public final void cleanupCache() {
        adminService.invalidateCaches();
    }

    @Before
    @BeforeEach
    public final void setupDirectory() throws IOException {
        this.basePath = Paths.get(appConfig.getBasePathStr());

        assertTrue(Files.exists(basePath));
    }

    @After
    @AfterEach
    public final void cleanupDirectory() throws IOException {
        clearBaseDirectoryContent();
        assertBaseDirectoryHasNoContent();
    }

    private void clearBaseDirectoryContent() throws IOException {
        File file = new File(basePath.toString());

        if (Files.exists(basePath))
            FileUtils.cleanDirectory(file);
    }

    private void assertBaseDirectoryHasNoContent() {
        try (Stream<Path> files = Files.list(basePath)) {
            assertEquals(0L, files.count());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

@TestConfiguration
class BaseTestConfiguration {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void initDB() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(con, new ClassPathResource("/sql/database_cleanup.sql"));
            ScriptUtils.executeSqlScript(con, new ClassPathResource("/sql/Reset_Sequences.sql"));
        }
    }
}
