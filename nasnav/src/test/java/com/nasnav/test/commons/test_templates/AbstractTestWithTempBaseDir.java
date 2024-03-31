package com.nasnav.test.commons.test_templates;

import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.dao.FilesRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.persistence.FileEntity;
import com.nasnav.persistence.OrganizationEntity;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;

@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG)
@TestPropertySource(locations = "classpath:test.database.properties")
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = BaseTestConfiguration.class)
public abstract class AbstractTestWithTempBaseDir {
    protected static final String TEST_PHOTO = "nasnav--Test_Photo.png";

    protected static final String TEST_IMG_DIR = "src/test/resources/test_imgs_to_upload";

    @Autowired
    protected OrganizationRepository orgRepo;

    @Autowired
    protected FilesRepository filesRepo;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AppConfig appConfig;

    protected Path basePath;


    @Autowired
    protected  MockMvc mockMvc;

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


    protected void uploadValidTestImg(
            String fileName,
            Long orgId,
            String sanitizedFileName,
            String expectedUrl,
            String userToken)
            throws Exception, IOException {
        Path expectedPath = Paths.get("" + orgId).resolve(sanitizedFileName);

        Path saveDir = basePath.resolve(orgId.toString());

        performFileUpload(fileName, orgId, userToken)
                .andExpect(status().is(200))
                .andExpect(content().string(expectedUrl));

        assertTrue("Organization directory was created", Files.exists(saveDir));

        try(Stream<Path> files = Files.list(saveDir)){
            assertNotEquals("new Files exists in the expected location", 0L, files.count() );
        }

        assertFileSavedToDb(fileName, orgId, expectedUrl, expectedPath);
    }

    protected ResultActions performFileUpload(String fileName, Long orgId, String userToken) throws IOException, Exception {
        String testImgDir = TEST_IMG_DIR;
        Path img = Paths.get(testImgDir).resolve(fileName).toAbsolutePath();
        assertTrue(Files.exists(img));
        byte[] imgData = Files.readAllBytes(img);

        String orgIdStr = orgId == null ? null : orgId.toString();
        MockMultipartFile file = new MockMultipartFile("file", fileName, "image/png", imgData);
        return mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
                .file(file)
                .header(TOKEN_HEADER, userToken)
                .param("org_id",  orgIdStr));
    }


    protected void assertFileSavedToDb(String fileName, Long orgId, String expectedUrl, Path expectedPath) {
        FileEntity file = filesRepo.findByUrl(expectedUrl);
        OrganizationEntity org = orgRepo.findOneById(orgId);

        assertNotNull("File meta-data was saved to database", file);
        assertEquals(expectedPath.toString().replace("\\", "/"), file.getLocation());
        assertEquals("image/png", file.getMimetype());
        assertEquals(org, file.getOrganization());
        assertEquals(fileName, file.getOriginalFileName());
    }


    /**
     * This is a workaround to fix the difference between postgres uri on developer host AND CI pipeline.
     */
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        if (isPostgresReachableOnLocalhost()) { // On Developer's Host
            registry.add("db.uri", () -> "jdbc:postgresql://localhost:5432/nasnav-tests");
            registry.add("spring.liquibase.url", () -> "jdbc:postgresql://localhost:5432/nasnav-tests");
        } else { //ON  CI pipeline
            registry.add("db.uri", () -> "jdbc:postgresql://postgres/nasnav-tests");
            registry.add("spring.liquibase.url", () -> "jdbc:postgresql://postgres/nasnav-tests");
        }
    }

    private static boolean isPostgresReachableOnLocalhost() {
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress("localhost", 5432), 5000);
            }
            return true;
        } catch (Exception ex) {
            return false;
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
