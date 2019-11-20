import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.nasnav.NavBox;
import com.nasnav.dao.FilesRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.persistence.FileEntity;
import com.nasnav.persistence.ProductImagesEntity;
import com.nasnav.security.AuthenticationFilter;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:database.properties")
@NotThreadSafe 
@ContextConfiguration(initializers = BaseDirInitialzer.class) //overrides the property "files.basepath" to use temp dir 
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) //creates a new context with new temp dir for each test method
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class ProductImgaeBulkUploadTest {
	private static final String PRODUCT_IMG_BULK_URL = "/product/image/bulk";

	private static final String USER_TOKEN = "101112";
	
	private static final String STORE_ADMIN_TOKEN = "ssErf33";
	private static final String OTHER_ORG_ADMIN_TOKEN = "131415";

	private static final String TEST_ZIP = "img_bulk_upload.zip";
	private static final String TEST_ZIP_NON_EXISTING_BARCODE ="img_bulk_upload_non_exisiting_barcode.zip";
	private static final String TEST_CSV = "img_bulk_barcode.csv";
	private static final String TEST_CSV_MULTI_BARCODE_PER_PATH = "img_bulk_multi_barcode_same_path.csv";
	private static final String TEST_ZIP_DIR = "src/test/resources/img_bulk_zip";

	private static final String TEST_ZIP_INVALID = "img_bulk_upload_invalid.zip";

	private static final String TEST_CSV_NON_EXISTING_BARCODE = "img_bulk_non_existing_barcode.csv";

	private static final String TEST_CSV_INCOMPLETE = "img_bulk_barcode_incomplete.csv";

	private static final String TEST_ZIP_EMPTY_IMG_FILE = "img_bulk_upload_empty_img_file.zip";

	private static final String TEST_ZIP_INTERNAL_STRUCT = "img_bulk_upload_internal_structure.zip";

	private static final String TEST_CSV_INTERNAL_STRUCT = "img_bulk_barcode_internal_struct.csv";

	private static final String TEST_ZIP_INTERNAL_STRUCT_WITH_BARCODE = "img_bulk_upload_internal_structure_with_barcode.zip";

	@Value("${files.basepath}")
	private String basePathStr;

	private Path basePath;
	
	
	@Autowired
	private FilesRepository filesRepo;
	
	@Autowired
	private ProductImagesRepository imgRepo;
	
	
	
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
	public void updateImgBulkNoAuthz() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_ZIP, TEST_CSV, jsonBytes, "NON-EXISTING-TOKEN")
	             .andExpect(status().is(401));
		
		assertNoImgsImported();
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkNoAuthN() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_ZIP, TEST_CSV, jsonBytes, STORE_ADMIN_TOKEN)
	             .andExpect(status().is(403));
		
		assertNoImgsImported();
	}
	
	
	
	
	@Test
	public void updateImgBulkNoZip() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		MockMultipartFile csvPart = createCsvPart(TEST_CSV);				
		MockPart jsonPart = createJsonPart(jsonBytes);
		
	    mockMvc.perform(MockMvcRequestBuilders.multipart(PRODUCT_IMG_BULK_URL)
							                 .file(csvPart)
							                 .part(jsonPart)
							                 .header(AuthenticationFilter.TOKEN_HEADER, USER_TOKEN))
	    		.andExpect(status().is(400));
	    
	    assertNoImgsImported();
	}
	
	
	
	
	
	@Test
	public void updateImgBulkMissingType() throws IOException, Exception {
		
		JSONObject json = createDummUploadRequest();
		json.remove("type");
		byte[] jsonBytes = json.toString().getBytes();
		
		performFileUpload(TEST_ZIP, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(406));
		
		assertNoImgsImported();
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkMissingPriority() throws IOException, Exception {
		
		JSONObject json = createDummUploadRequest();
		json.remove("priority");
		
		byte[] jsonBytes = json.toString().getBytes();
		
		performFileUpload(TEST_ZIP, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(406));	
		
		assertNoImgsImported();
	}
	
	
	
	
	@Test
	public void updateImgBulkEmptyZip() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		MockMultipartFile zipPart = createEmptyZipPart(TEST_ZIP);	
		MockMultipartFile csvPart = createCsvPart(TEST_CSV);				
		MockPart jsonPart = createJsonPart(jsonBytes);
		
	    mockMvc.perform(MockMvcRequestBuilders.multipart(PRODUCT_IMG_BULK_URL)
	    									 .file(zipPart)
							                 .file(csvPart)
							                 .part(jsonPart)
							                 .header(AuthenticationFilter.TOKEN_HEADER, USER_TOKEN))
	    		.andExpect(status().is(406));
	    
	    assertNoImgsImported();
	}
	
	
	
	
	@Test
	public void updateImgBulkNotZipFile() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_CSV, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(406));
		
		assertNoImgsImported();
	}
	
	
	
	
	@Test
	public void updateImgBulkInvalidZipFile() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		//if the zip file is invalid , nothing will be read from it , but
		//no exception is thrown.
		performFileUpload(TEST_ZIP_INVALID, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200));		
		
		assertNoImgsImported();
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkTestInvalidBarcode() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_ZIP_NON_EXISTING_BARCODE, TEST_CSV_NON_EXISTING_BARCODE, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(500))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		JSONObject errorResponse = new JSONObject(response);
		JSONArray errors = new JSONArray( errorResponse.getString("error") );
		
		assertTrue(errorResponse.has("error"));
		assertEquals(1, errors.length());
		
		
		assertNoImgsImported();
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkAdminOfOtherOrg() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_ZIP, TEST_CSV, jsonBytes, OTHER_ORG_ADMIN_TOKEN)
	             .andExpect(status().is(500))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		JSONObject errorResponse = new JSONObject(response);
		JSONArray errors = new JSONArray( errorResponse.getString("error") );
		
		assertTrue(errorResponse.has("error"));
		assertEquals(2, errors.length());
		
		
		assertNoImgsImported();
	}
	
	
	
	
	
	@Test
	public void updateImgBulkWithCSVTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_ZIP, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		assertImgsImported(response);
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkWithCSVAndMultipleBarcodeForSamePathTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_ZIP, TEST_CSV_MULTI_BARCODE_PER_PATH, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		assertSameImgImportedForProductAndVariants(response);
	}
	
	
	
	
	
	@Test
	public void updateImgBulkWithCSVAndInternalStructureTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		//test adding images inside internal folders of the ZIP file.
		//The path of images in the CSV may start with '/' or not.
		String response = 
				performFileUpload(TEST_ZIP_INTERNAL_STRUCT, TEST_CSV_INTERNAL_STRUCT, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		assertImgsImported(response);
	}
	
	
	
	
	
	@Test
	public void updateImgBulkNoCSVAndInternalStructureTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		String response = 
				performFileUploadNoCSV(TEST_ZIP_INTERNAL_STRUCT_WITH_BARCODE, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		assertImgsImported(response);
	}
	
	
	
	
	
	
	
	
	
	@Test
	public void updateImgBulkNoCSVTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		String response = performFileUploadNoCSV(TEST_ZIP, jsonBytes, USER_TOKEN)
									.andExpect(status().is(200))
						    		.andReturn()
						            .getResponse()
						            .getContentAsString();	
		
		assertImgsImported(response);
	}
	
	
	
	
	
	

	@Test
	public void updateImgBulkWithIncompleteCSVTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_ZIP, TEST_CSV_INCOMPLETE, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		assertImgsImported(response);
	}
	
	
	

	
	
	@Test
	public void updateImgBulkTestEmptyImgFile() throws IOException, Exception {
		
		byte[] jsonBytes = createDummUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_ZIP_EMPTY_IMG_FILE, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(500))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		JSONObject errorResponse = new JSONObject(response);
		JSONArray errors = new JSONArray( errorResponse.getString("error") );
		
		assertTrue(errorResponse.has("error"));
		assertEquals(1, errors.length());
		
		
		assertNoImgsImported();
	}
	
	
	
	
	

	private void assertImgsImported(String response) {
		JSONArray responseJson = new JSONArray(response);
		assertEquals(
				"import 2 images, one of them have a barcode that is used by both a product and a variant, so the 2 images are imported as 3 records"
				, 3 
				, responseJson.length());				
		
		assertEquals( 3L, imgRepo.count());		
		
		IntStream.range(0, responseJson.length())
				.mapToObj(responseJson::getJSONObject)
				.forEach(this::assertImageUploaded);
	}
	
	
	
	
	
	
	private void assertSameImgImportedForProductAndVariants(String response) {
		JSONArray responseJson = new JSONArray(response);
		assertEquals(
				"import 2 images, one of them have a barcode that is used by both a product and a variant, so the 2 images are imported as 3 records"
				, 3 
				, responseJson.length());				
		
		assertEquals( 3L, imgRepo.count());		
		
		IntStream.range(0, responseJson.length())
				.mapToObj(responseJson::getJSONObject)
				.forEach(this::assertImageUploaded);
		
		
		long urlCount = IntStream.range(0, responseJson.length())
				.mapToObj(responseJson::getJSONObject)
				.map(obj -> obj.getString("image_url"))
				.distinct()
				.count();

		assertEquals("a single image was imported for the product and its variants, so they all should reference the same url"
						, 1L, urlCount);
	}
	

	
	
	
	
	
	private void assertNoImgsImported() throws IOException {
		assertEquals(0L, imgRepo.count());
		assertEquals(0L, filesRepo.count());
		try(Stream<Path> files = Files.walk(basePath)){
			Long cnt = files.filter(path -> !Files.isDirectory(path) ).count();
			assertEquals("no files should exist in the save directory", 0L, cnt.longValue());
		}
	}



	
	

	private ResultActions performFileUploadNoCSV(String zipFileName, byte[] json, String userToken)
			throws UnsupportedEncodingException, Exception {
		MockMultipartFile zipPart = createZipPart(zipFileName);		
		MockPart jsonPart = createJsonPart(json);
		
		return	mockMvc.perform(MockMvcRequestBuilders.multipart(PRODUCT_IMG_BULK_URL)
	    									 .file(zipPart)
							                 .part(jsonPart)
							                 .header(AuthenticationFilter.TOKEN_HEADER, USER_TOKEN))
			    		;
	}
	
	
	
	
	
	private void assertImageUploaded(JSONObject response) {
		Long imgId = response.getLong("image_id");
		String imgUrl = response.getString("image_url");
		
		FileEntity fileEntity = filesRepo.findByUrl(imgUrl);
		Path imgPath = basePath.resolve(fileEntity.getLocation());
		
		assertTrue(imgRepo.existsById(imgId));		
		assertTrue(Files.exists(imgPath));
	}
	
	
	
	
	private ResultActions performFileUpload(String zipFileName, String csvFileName, byte[] json, String userToken) throws IOException, Exception {
				
		MockMultipartFile zipPart = createZipPart(zipFileName);		
		MockMultipartFile csvPart = createCsvPart(csvFileName);				
		MockPart jsonPart = createJsonPart(json);
		
		ResultActions result = 
		    mockMvc.perform(MockMvcRequestBuilders.multipart(PRODUCT_IMG_BULK_URL)
								                 .file(zipPart)
								                 .file(csvPart)
								                 .part(jsonPart)
								                 .header(AuthenticationFilter.TOKEN_HEADER, userToken));
		return result;
	}




	private MockPart createJsonPart(byte[] json) {
		MockPart jsonPart = new MockPart("properties", "properties",  json);		
		jsonPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		return jsonPart;
	}




	private MockMultipartFile createCsvPart(String csvFileName) throws IOException {
		Path csv = Paths.get(TEST_ZIP_DIR).resolve(csvFileName).toAbsolutePath();
		assertTrue(Files.exists(csv));
		byte[] csvData = Files.readAllBytes(csv);		
		MockMultipartFile csvPart = new MockMultipartFile("imgs_barcode_csv", csvFileName, "text/csv", csvData);
		return csvPart;
	}




	private MockMultipartFile createZipPart(String zipFileName) throws IOException {
		Path zip = Paths.get(TEST_ZIP_DIR).resolve(zipFileName).toAbsolutePath();
		assertTrue(Files.exists(zip));
		byte[] zipData = Files.readAllBytes(zip);		
		MockMultipartFile zipPart = new MockMultipartFile("imgs_zip", zipFileName, "application/zip", zipData);
		return zipPart;
	}
	
	
	
	
	
	private MockMultipartFile createEmptyZipPart(String zipFileName) throws IOException {		
		byte[] zipData = new byte[0];		
		MockMultipartFile zipPart = new MockMultipartFile("imgs_zip", zipFileName, "application/zip", zipData);
		return zipPart;
	}
	
	
	
	
	
	
	private JSONObject createDummUploadRequest() {
		JSONObject metaData = new JSONObject();		
		
		metaData.put("type", 7);
		metaData.put("priority", 1);
		
		return metaData;
	}
}
