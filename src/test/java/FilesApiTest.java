import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.nasnav.NavBox;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc 
@PropertySource("classpath:database.properties")
@ContextConfiguration(initializers = BaseDirInitialzer.class) //overrides the property "files.basepath" to use temp dir 
@NotThreadSafe
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) //creates a new context with new temp dir for each test method
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Files_API_Test_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/Files_API_Test_Delete.sql"})
public class FilesApiTest {
	private static final String TEST_IMG_DIR = "src/test/resources/test_imgs_to_upload";

	@Value("${files.basepath}")
	private String basePathStr;

	private Path basePath;
	
	
	@Autowired
	private  MockMvc mockMvc;
	
	
	@Before
	public void setup() throws IOException {		
		this.basePath = Paths.get(basePathStr);
		
		System.out.println("Test Files Base Path  >>>> " + basePath.toAbsolutePath());
		
		//The base directory must exists for all tests
		assertTrue(Files.exists(basePath));
		
		//assert an empty temp directory was created for the test
		try(Stream<Path> files = Files.list(basePath)){
			assertEquals(0L, files.count());
		}
		
	}




	
	
	
	
	
	@Test
	public void fileUploadTest() throws Exception {
		
		String fileName = "myPhoto.png";
		Long orgId = 99001L;
		Path saveDir = basePath.resolve(orgId.toString());		
		
		performFileUpload(fileName, orgId)
             .andExpect(status().is(200))
             .andExpect(content().string(fileName));
		 
		 assertTrue("Ogranization directory was created", Files.exists(saveDir));
		 
		 try(Stream<Path> files = Files.list(saveDir)){
			 assertNotEquals("new Files exists in the expected location", 0L, files.count() );
		 }
		 
	}
	
	
	
	@Test
	public void fileUploadSameNameTest() throws Exception {
		
		String fileName = "myPhoto.png";
		Long orgId = 99001L;
		Path saveDir = basePath.resolve(orgId.toString());		
		
		performFileUpload(fileName, orgId)
             .andExpect(status().is(200))
             .andExpect(content().string(fileName));
		
		//second upload, expect the file name to be modified
		String fileNameNoExt = com.google.common.io.Files.getNameWithoutExtension(fileName);
		performFileUpload(fileName, orgId)
        .andExpect(status().is(200))
        .andExpect(content().string(startsWith(fileNameNoExt)));
		 
		 assertTrue("Ogranization directory was created", Files.exists(saveDir));
		 
		 try(Stream<Path> files = Files.list(saveDir)){
			 assertEquals("2 Files exists in the expected location", 2L, files.count() );
		 }
		 
	}
	
	
	
	
	
	@Test
	public void fileUploadNullOrgTest() throws Exception {
		
		String fileName = "myPhoto.png";
		Long orgId = null;
		Path saveDir = basePath;		
		
		performFileUpload(fileName, orgId)
             .andExpect(status().is(200))
             .andExpect(content().string(fileName));
		 
		 assertTrue("Save directory was created", Files.exists(saveDir));
		 
		 Path expectedPath = saveDir.resolve(fileName);		 
		 assertTrue("saved file exists in the expected location", Files.exists(expectedPath) );		 
	}




	private ResultActions performFileUpload(String fileName, Long orgId) throws IOException, Exception {
		String testImgDir = TEST_IMG_DIR;
		Path img = Paths.get(testImgDir).resolve(fileName).toAbsolutePath();			
		assertTrue(Files.exists(img));		
		byte[] imgData = Files.readAllBytes(img);
		
		String orgIdStr = orgId == null ? null : orgId.toString();
		 MockMultipartFile file = new MockMultipartFile("file", fileName, "image/png", imgData);
		 ResultActions result = 
		    mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
                 .file(file)
                 .param("org_id",  orgIdStr));
		return result;
	}
	
	/**
	 * TODO:
	 * * - test database save
	 * - test null org [DONE]
	 * - test org with existing dir
	 * - test non-existing org
	 * - test non-existing file, file with no name
	 * - test upload image multiple times (name rename)[DONE]
	 * - 
	 * */
	
	
	
	

}


//overrides the property "files.basepath" to use temp dir
class BaseDirInitialzer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
			setBasePathAsTempDir(applicationContext);
	}
	
	
	public static void setBasePathAsTempDir(ConfigurableApplicationContext applicationContext) {
		Path tempDirPath;
		try {
			tempDirPath = Files.createTempDirectory("_nasnav_test_");
		} catch (IOException e) {			
			e.printStackTrace();
			tempDirPath = Paths.get("src/test/resources/test_files_base_dir");
		}	
		
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
		        "files.basepath=" + tempDirPath.toString().replace("\\", "/") );
	}

}






