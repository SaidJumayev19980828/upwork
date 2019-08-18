import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.nasnav.NavBox;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.FilesRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.persistence.FileEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.security.AuthenticationFilter;

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
	private static final String TEST_PHOTO = "nasnav--Test_Photo.png";

	private static final String TEST_IMG_DIR = "src/test/resources/test_imgs_to_upload";

	@Value("${files.basepath}")
	private String basePathStr;

	private Path basePath;
	
	
	
	@Autowired
	private FilesRepository filesRepo;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
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
		
		String fileName = TEST_PHOTO;
		Long orgId = 99001L;
		
		String sanitizedFileName = StringUtils.getFileNameSanitized(fileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		uploadValidTestImg(fileName, orgId, sanitizedFileName, expectedUrl);		 
	}
	
	
	
	
	
	@Test
	public void fileUploadOrgNotExistTest() throws Exception {
		
		String fileName = TEST_PHOTO;
		Long orgId = 99123L;
		
		
		Path saveDir = basePath.resolve(orgId.toString());		
		
		performFileUpload(fileName, orgId)
		.andExpect(status().is(406));
		 
		assertFalse("Save directory was NOT created", Files.exists(saveDir));
		 
		assertEquals("Nothing saved to DB", 0L, filesRepo.count());		 
	}









	private void assertFileSavedToDb(String fileName, Long orgId, String expectedUrl, Path expectedPath) {
		FileEntity file = filesRepo.findByUrl(expectedUrl);	
		OrganizationEntity org = orgRepo.findOneById(orgId);
		 
		 assertNotNull("File meta-data was saved to database", file);
		 assertEquals(expectedPath.toString().replace("\\", "/"), file.getLocation());
		 assertEquals("image/png", file.getMimetype());
		 assertEquals(org, file.getOrganization());
		 assertEquals(fileName, file.getOriginalFileName());
	}
	
	
	
	@Test
	public void fileUploadSameNameTest() throws Exception {
		
		String fileName = TEST_PHOTO;
		Long orgId = 99001L;
		
		String sanitizedFileName = StringUtils.getFileNameSanitized(fileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		
		Path saveDir = basePath.resolve(orgId.toString());		
		
		performFileUpload(fileName, orgId)
             .andExpect(status().is(200))
             .andExpect(content().string(expectedUrl));
		
		//second upload, expect the file name to be modified
		String fileUrlNoExt = orgId + "/" + StringUtils.stripFilenameExtension(sanitizedFileName);
		String secondFileUrl = performFileUpload(fileName, orgId)
							        .andExpect(status().is(200))
							        .andExpect(content().string(startsWith(fileUrlNoExt)))
							        .andReturn()
							        .getResponse()
							        .getContentAsString();
		
		assertNotEquals("each File should have unique url",expectedUrl, secondFileUrl);
		 
		 assertTrue("Ogranization directory was created", Files.exists(saveDir));
		 
		 try(Stream<Path> files = Files.list(saveDir)){
			 assertEquals("2 Files exists in the expected location", 2L, files.count() );
		 }
		 
		 assertEquals("Both files saved to DB", 2L, filesRepo.count());
		 
	}
	
	
	
	
	
	@Test
	public void fileUploadNullOrgTest() throws Exception {
		
		String fileName = TEST_PHOTO;
		Long orgId = null;
		
		String sanitizedFileName = StringUtils.getFileNameSanitized(fileName);		
		String expectedUrl =  sanitizedFileName;
		Path expectedPath = Paths.get(sanitizedFileName);
		
		Path saveDir = basePath;		
		
		performFileUpload(fileName, orgId)
             .andExpect(status().is(200))
             .andExpect(content().string(expectedUrl));
		 
		 assertTrue("Save directory was created", Files.exists(saveDir));
		 
		 Path expectedAbsPath = basePath.resolve(sanitizedFileName);		 
		 assertTrue("saved file exists in the expected location", Files.exists(expectedAbsPath) );	
		 
		 assertFileSavedToDb(fileName, orgId, expectedUrl, expectedPath);
	}
	
	
	
	
	
	@Test
	public void fileUploadNoName() throws Exception {		
		Long orgId = 99001L;		
		
		Path saveDir = basePath.resolve(orgId.toString());	
		
		String orgIdStr = orgId == null ? null : orgId.toString();
		 MockMultipartFile file = new MockMultipartFile("file", "", "image/png", new byte[0]);		 
		    mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
                 .file(file)
                 .header(AuthenticationFilter.TOKEN_HEADER, "101112")
                 .param("org_id",  orgIdStr))
             .andExpect(status().is(406));
		 
		assertFalse("Save directory was NOT created", Files.exists(saveDir));
		 
		assertEquals("Nothing saved to DB", 0L, filesRepo.count());
	}
	
	
	
	
	@Test
	public void fileUploadNoData() throws Exception {	
		String fileName = TEST_PHOTO;
		Long orgId = 99001L;		
				
		String sanitizedFileName = StringUtils.getFileNameSanitized(fileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		Path expectedPath = Paths.get(""+orgId).resolve(sanitizedFileName);
		Path saveDir = basePath.resolve(orgId.toString());	
		
		String orgIdStr = orgId == null ? null : orgId.toString();
		MockMultipartFile file = new MockMultipartFile("file", fileName, "image/png", new byte[0]);		 
		    mockMvc.perform(
		    		MockMvcRequestBuilders.multipart("/files")
		                 .file(file)
		                 .header(AuthenticationFilter.TOKEN_HEADER, "101112")
		                 .param("org_id",  orgIdStr))
		    .andExpect(status().is(200))
            .andExpect(content().string(expectedUrl));
		 
		 assertTrue("Ogranization directory was created", Files.exists(saveDir));
		 
		 try(Stream<Path> files = Files.list(saveDir)){
			 assertNotEquals("File with size 0 KB  exists in the expected location", 0L, files.count() );
		 }
		 
		 assertFileSavedToDb(fileName, orgId, expectedUrl, expectedPath);
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
								                 .header(AuthenticationFilter.TOKEN_HEADER, "101112")
								                 .param("org_id",  orgIdStr));
		return result;
	}
	
	
	
	
	
	@Test
	public void downloadFileTest() throws IOException, Exception {
		//first upload a file 
		
		String fileName = TEST_PHOTO;
		Long orgId = 99001L;
		Path origFile = Paths.get(TEST_IMG_DIR).resolve(fileName).toAbsolutePath();	
		
		
		String sanitizedFileName = StringUtils.getFileNameSanitized(fileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		
		uploadValidTestImg(fileName, orgId, sanitizedFileName, expectedUrl);
		
		 //--------------------------------------------
		 //Now try to download the file
		 
		 MvcResult result = mockMvc.perform( 
				 						MockMvcRequestBuilders.get("/files/"+ expectedUrl)
				 								.header(AuthenticationFilter.TOKEN_HEADER, "101112")
				 								.contentType(MediaType.ALL_VALUE)				 
				 				)
 								.andExpect(status().is(200))
 								.andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
 								.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(fileName)))
 								.andReturn();
	     
		 Path downloadedFile = Files.createTempFile( StringUtils.stripFilenameExtension(fileName), ".png");
		 InputStream in = new ByteArrayInputStream(result.getResponse().getContentAsByteArray());
		 Files.copy(in, downloadedFile, StandardCopyOption.REPLACE_EXISTING );
		 System.out.println("Downloaded File >>>> " + downloadedFile);
		 
	     assertEquals(Files.size(origFile), Files.size(downloadedFile));
         assertEquals("image/png", result.getResponse().getContentType());
	}





	
	@Test
	public void downloadFileUrlNotExists() throws Exception {		
		 mockMvc.perform( 
 						MockMvcRequestBuilders.get("/files/NON_EXISTING")
 								.header(AuthenticationFilter.TOKEN_HEADER, "101112")
 								.contentType(MediaType.ALL_VALUE)				 
 				)
				.andExpect(status().is(406))
				.andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION));	 
	}
	
	
	
	@Test
	public void downloadFileUrlInvalid() throws Exception {		
		 mockMvc.perform( 
 						MockMvcRequestBuilders.get("/files")
 								.header(AuthenticationFilter.TOKEN_HEADER, "101112")
 								.contentType(MediaType.ALL_VALUE)				 
 				)
				.andExpect(status().is(406))
				.andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION));	 
	}
	
	
	
	
	@Test
	public void downloadFileNoAuthN() throws Exception {		
		 mockMvc.perform( 
 						MockMvcRequestBuilders.get("/files")
 								.contentType(MediaType.ALL_VALUE)				 
 				)
				.andExpect(status().is(401))
				.andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION));	 
	}
	
	
	
	
	@Test
	public void downloadFileDeletedOnSystem() throws Exception {		
		//first upload a file 
		
		String fileName = TEST_PHOTO;
		Long orgId = 99001L;		
		
		String sanitizedFileName = StringUtils.getFileNameSanitized(fileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		
		uploadValidTestImg(fileName, orgId, sanitizedFileName, expectedUrl);
		
		//----------------------------------------------
		
		Path uploadedFile = basePath.resolve(""+orgId).resolve(sanitizedFileName);
		Files.delete(uploadedFile);
		
		 //--------------------------------------------
		 //Now try to download the deleted file
		 
		mockMvc.perform( 
					MockMvcRequestBuilders.get("/files/"+ expectedUrl)
							.header(AuthenticationFilter.TOKEN_HEADER, "101112")
							.contentType(MediaType.ALL_VALUE)				 
			)
		.andExpect(status().is(406))
		.andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION));	 
	}




	private void uploadValidTestImg(String fileName, Long orgId, String sanitizedFileName, String expectedUrl)
			throws Exception, IOException {
		Path expectedPath = Paths.get(""+orgId).resolve(sanitizedFileName);
		
		Path saveDir = basePath.resolve(orgId.toString());		
		
		performFileUpload(fileName, orgId)
             .andExpect(status().is(200))
             .andExpect(content().string(expectedUrl));
		 
		 assertTrue("Ogranization directory was created", Files.exists(saveDir));
		 
		 try(Stream<Path> files = Files.list(saveDir)){
			 assertNotEquals("new Files exists in the expected location", 0L, files.count() );
		 }
		 
		 assertFileSavedToDb(fileName, orgId, expectedUrl, expectedPath);
	}

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






