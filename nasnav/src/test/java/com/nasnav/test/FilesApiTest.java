package com.nasnav.test;

import com.nasnav.AppConfig;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.FilesRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.persistence.FileEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.nasnav.commons.utils.StringUtils.getFileNameSanitized;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Files_API_Test_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class FilesApiTest extends AbstractTestWithTempBaseDir {
	private static final String TEST_PHOTO = "nasnav--Test_Photo.png";

	private static final String TEST_IMG_DIR = "src/test/resources/test_imgs_to_upload";


	@Autowired
	private FilesRepository filesRepo;

	@Autowired
	private AppConfig appConfig;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	@Autowired
	private  MockMvc mockMvc;
	
	@Autowired
	private TestRestTemplate template;

	@Autowired
	private WebApplicationContext webApplicationContext;

	
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
                 .header(TOKEN_HEADER, "101112")
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
		                 .header(TOKEN_HEADER, "101112")
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
		return mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
											 .file(file)
											 .header(TOKEN_HEADER, "101112")
											 .param("org_id",  orgIdStr));
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

 		ResponseEntity<byte[]> response = template.exchange("/files/"+ expectedUrl, GET, getHttpEntity(""), byte[].class);
 		assertEquals(OK, response.getStatusCode());
	    assertEquals(Files.size(origFile), response.getBody().length);
	    assertEquals("image/png", response.getHeaders().getContentType().toString());
	}





	
	@Test
	public void downloadFileUrlNotExists() throws Exception {		
		 mockMvc.perform( 
 						MockMvcRequestBuilders.get("/files/NON_EXISTING") 								
 								.contentType(MediaType.ALL_VALUE)				 
 				)
				.andExpect(status().is(404))
				.andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION));	 
	}
	
	
	
	@Test
	public void downloadFileUrlInvalid() throws Exception {		
		 mockMvc.perform( 
 						MockMvcRequestBuilders.get("/files") 								
 								.contentType(MediaType.ALL_VALUE)				 
 				)
				.andExpect(status().is(406))
				.andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION));	 
	}
	
	
	
	
	
	@Test
	public void downloadFileDeletedOnSystem() throws Exception {
		//first upload a file
		String fileName = TEST_PHOTO;
		Long orgId = 99001L;		
		
		String sanitizedFileName = getFileNameSanitized(fileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		
		uploadValidTestImg(fileName, orgId, sanitizedFileName, expectedUrl);
		
		//----------------------------------------------
		// to get cached in spring resource cache
		ResponseEntity<String> response = template.exchange("/files/"+ expectedUrl, GET, getHttpEntity(""), String.class);
		assertEquals(OK, response.getStatusCode());
		//--------------------------------------------
		// still in cache
		Path uploadedFile = basePath.resolve(""+orgId).resolve(sanitizedFileName);
		Files.delete(uploadedFile);
		
		 //--------------------------------------------
		 // previous request shouldn't affect this request
		response = template.exchange("/files/"+ expectedUrl, GET, getHttpEntity(""), String.class);
		assertEquals(NOT_FOUND, response.getStatusCode());
	}




	private void uploadValidTestImg(String fileName, Long orgId, String sanitizedFileName, String expectedUrl)
			throws Exception, IOException {
		Path expectedPath = Paths.get(""+orgId).resolve(sanitizedFileName);
		
		Path saveDir = basePath.resolve(orgId.toString());		
		
		performFileUpload(fileName, orgId)
             .andExpect(status().is(200))
             .andExpect(content().string(expectedUrl));
		 
		 assertTrue("Organization directory was created", Files.exists(saveDir));
		 
		 try(Stream<Path> files = Files.list(saveDir)){
			 assertNotEquals("new Files exists in the expected location", 0L, files.count() );
		 }
		 
		 assertFileSavedToDb(fileName, orgId, expectedUrl, expectedPath);
	}

	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/UserRegisterTest.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void uploadFileTestWithInvalidMimes() throws Exception {
		List<MockMultipartFile> invalidFiles = getInvalidFiles();

		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		for (MockMultipartFile file : invalidFiles) {
			mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
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
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/UserRegisterTest.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void uploadFileTestWithValidMimes() throws Exception {
		List<MockMultipartFile> validFiles = getValidFiles();

		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		for (MockMultipartFile file : validFiles) {
			mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
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






