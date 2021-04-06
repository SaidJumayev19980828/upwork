package com.nasnav.test;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
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
import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.NavBox;
import com.nasnav.dao.FilesRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.persistence.FileEntity;
import com.nasnav.test.bulkimport.img.ImageBulkUrlUploadTestCommon;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:test.database.properties")
@NotThreadSafe 
@ContextConfiguration(initializers = BaseDirInitialzer.class) //overrides the property "files.basepath" to use temp dir 
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) //creates a new context with new temp dir for each test method
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@Ignore  //tests are too slow for now
public class ProductImageBulkUrlUploadTest {
	private static final String PRODUCT_IMG_BULK_URL = "/product/image/bulk/url";

	private static final String USER_TOKEN = "101112";	
	private static final String STORE_ADMIN_TOKEN = "ssErf33";
	private static final String OTHER_ORG_ADMIN_TOKEN = "131415";
	
	private static final String TEST_ZIP_DIR = "src/test/resources/img_bulk_zip";
	private static final String TEST_CSV = "img_bulk_url_barcode.csv";
	private static final String TEST_CSV_IMG_NOT_FOUND ="img_bulk_url_barcode_img_not_found.csv";
	private static final String TEST_CSV_NON_EXISTING_BARCODE = "img_bulk_url_non_existing_barcode.csv";
	private static final String TEST_CSV_MULTI_BARCODE_PER_PATH = "img_bulk_url_multi_barcode_same_path.csv";
	private static final String TEST_CSV_VARIANT_ID_EXISTING_VARIANT = "img_bulk_url_exist_variant_id_exist_variant.csv";
	private static final String TEST_CSV_EXTERNAL_ID_EXISTING_MAPPING = "img_bulk_url_exist_external_id_exist_mapping.csv";
	private static final String TEST_CSV_VARIANT_ID_NO_VARIANT = "img_bulk_url_exist_variant_id_no_variant.csv";
	private static final String TEST_CSV_EXTERNAL_ID_NO_MAPPING = "img_bulk_url_exist_external_id_no_mapping.csv";
	
	
	
	@Value("${files.basepath}")
	private String basePathStr;

	private Path basePath;
	
	
	@Autowired
	private FilesRepository filesRepo;
	
	@Autowired
	private ProductImagesRepository imgRepo;
	
	
	@Autowired
	private ImageBulkUrlUploadTestCommon testCommons;
	
	@Autowired
	private  MockMvc mockMvc;
	
	
	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this,8188);
	
	
	private String serverUrl;
	
	
	@Before
	public void setup() throws Exception {		
		assertRequiredDirectoriesExists();
		
		
		serverUrl = testCommons.initImgsMockServer(mockServerRule);
	}



	private void assertRequiredDirectoriesExists() throws IOException {
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
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_CSV, jsonBytes, "NON-EXISTING-TOKEN")
	             .andExpect(status().is(401));
		
		assertNoImgsImported();
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkNoAuthN() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_CSV, jsonBytes, STORE_ADMIN_TOKEN)
	             .andExpect(status().is(403));
		
		assertNoImgsImported();
	}
	
	
	
	
	@Test
	public void updateImgBulkMissingType() throws IOException, Exception {
		
		JSONObject json = createDummyUploadRequest();
		json.remove("type");
		byte[] jsonBytes = json.toString().getBytes();
		
		performFileUpload(TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(406));
		
		assertNoImgsImported();
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkMissingPriority() throws IOException, Exception {
		
		JSONObject json = createDummyUploadRequest();
		json.remove("priority");
		
		byte[] jsonBytes = json.toString().getBytes();
		
		performFileUpload(TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(406));	
		
		assertNoImgsImported();
	}
	
	
	
	
	
	@Test
	public void updateImgBulkTestInvalidBarcode() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();

		performFileUpload(TEST_CSV_NON_EXISTING_BARCODE, jsonBytes, USER_TOKEN)
         .andExpect(status().is(406))
         .andReturn()
         .getResponse()
         .getContentAsString();

		assertNoImgsImported();
	}
	
	
	
	@Test
	public void updateImgBulkAdminOfOtherOrg() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_CSV, jsonBytes, OTHER_ORG_ADMIN_TOKEN)
         .andExpect(status().is(406))
         .andReturn()
         .getResponse()
         .getContentAsString();

		assertNoImgsImported();
	}
	
	
	
	
	
	@Test
	public void updateImgBulkWithCSVTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();
		
		assertImgsImported(response);		
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void updateImgBulkWithCSVAndDeleteOldImagesTest() throws IOException, Exception {
		JSONObject request = createDummyUploadRequest();
		request.put("delete_old_images", true);
		byte[] jsonBytes = request.toString().getBytes();
		
		validateImageCountBefore();
		
		String response = 
				performFileUpload(TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		assertImgsImportedAndCollectionImagesRemained(response);
		
		validateImageCountAfter(3L);
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void updateImgBulkWithCSVAndDeleteOldImagesDisabledTest() throws IOException, Exception {
		JSONObject request = createDummyUploadRequest();
		byte[] jsonBytes = request.toString().getBytes();
		
		validateImageCountBefore();
		
		String response = 
				performFileUpload(TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();
		
		assertImgsImported(response, 5L);
		
		validateImageCountAfter(5L);
	}



	private void validateImageCountBefore() {
		Long countBefore = imgRepo.countByProductEntity_OrganizationId(99001L);
		assertEquals(3L, countBefore.longValue());
		Long countOtherOrgBefore = imgRepo.countByProductEntity_OrganizationId(99002L);
		assertEquals(1L, countOtherOrgBefore.longValue());
	}



	private void validateImageCountAfter(Long expectedCount) {
		Long countAfter = imgRepo.countByProductEntity_OrganizationId(99001L);
		assertEquals("only two images were imported, and old images should have been delete", expectedCount, countAfter);
		
		Long countOtherOrgAfter = imgRepo.countByProductEntity_OrganizationId(99002L);
		assertEquals(1L, countOtherOrgAfter.longValue());
	}
	
	
	
	
	@Test
	public void updateImgBulkWithCSVErrorFetchingImageTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_CSV_IMG_NOT_FOUND, jsonBytes, USER_TOKEN)
         .andExpect(status().is(200))
         .andReturn()
         .getResponse()
         .getContentAsString();
		
		assertNoImgsImported();		
	}
	
	
	
	
	

	/**
	 * just a test that the local test server is returning static test images for the 
	 * rest of tests.
	 * */
	@Test
	public void getImgFromLocalServerTest() throws IOException, Exception {
		
		WebClient client = WebClient.builder().baseUrl(serverUrl).build();
		client
		 .get()
		 .uri("/static/test_photo_2.png")
		 .exchange()
		 .doOnNext(res -> assertEquals(OK, res.statusCode()))
		 .flatMapMany(res -> res.bodyToMono(byte[].class))
		 .subscribe(body -> System.out.println(">>>" + body.length));
		
		Thread.sleep(1000);
	}
	
	
	
	
	
	
	
	@Test
	public void updateImgBulkWithCSVAndMultipleBarcodeForSamePathTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_CSV_MULTI_BARCODE_PER_PATH, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		assertSameImgImportedForProductAndVariants(response);
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert_2.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void updateImgBulkDuplicatePathForVariantsOfSameProductTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_CSV_MULTI_BARCODE_PER_PATH, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		assertSameImgImportedForProductWithNoDuplicates(response);
	}
	
	
	
	

	
	
	@Test
	public void updateImgBulkTestExistVariantIdExistVariantEntity() throws IOException, Exception {
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();

		String response =
				performFileUpload(TEST_CSV_VARIANT_ID_EXISTING_VARIANT, jsonBytes, USER_TOKEN)
						.andExpect(status().is(200))
						.andReturn()
						.getResponse()
						.getContentAsString();

		assertImgsImported(response);
	}

	
	
	
	
	
	@Test
	public void updateImgBulkTestExistExternalIdExistMapping() throws IOException, Exception {
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();

		String response =
				performFileUpload(TEST_CSV_EXTERNAL_ID_EXISTING_MAPPING, jsonBytes, USER_TOKEN)
						.andExpect(status().is(200))
						.andReturn()
						.getResponse()
						.getContentAsString();

		assertImgsImported(response);
	}

	
	
	
	
	@Test
	public void updateImgBulkTestExistVariantIdNoVariant() throws Exception {
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();

		performFileUpload(TEST_CSV_VARIANT_ID_NO_VARIANT, jsonBytes, USER_TOKEN)
				.andExpect(status().is(406));

		assertNoImgsImported();
	}

	
	
	@Test
	public void updateImgBulkTestExistVariantIdNoVariantButIgnoreErrors() throws Exception {
		byte[] jsonBytes = 
				createDummyUploadRequest()
					.put("ignore_errors", true)
					.toString()
					.getBytes();

		performFileUpload(TEST_CSV_VARIANT_ID_NO_VARIANT, jsonBytes, USER_TOKEN)
				.andExpect(status().is(200));

		assertNoImgsImported();
	}
	
	
	
	@Test
	public void updateImgBulkTestExistExternalIdNoMapping() throws Exception {
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();

		performFileUpload(TEST_CSV_EXTERNAL_ID_NO_MAPPING, jsonBytes, USER_TOKEN)
				.andExpect(status().is(406));

		assertNoImgsImported();
	}
	
	
	
	private void assertSameImgImportedForProductAndVariants(String response) {
		JSONArray responseJson = new JSONArray(response);
		assertEquals(
				"import 2 images"
				, 2
				, responseJson.length());				
		
		assertEquals( 2L, imgRepo.count());		
		
		IntStream.range(0, responseJson.length())
				.mapToObj(responseJson::getJSONObject)
				.forEach(this::assertImageUploaded);
		
		List<String> urls = 
				IntStream.range(0, responseJson.length())
						.mapToObj(responseJson::getJSONObject)
						.map(obj -> obj.getString("image_url"))
						.distinct()
						.collect(toList());
		assertEquals("a single image was imported for the product and its variants, so they all should reference the same url"
						, 1, urls.size());
	}
	
	
	
	
	
	private void assertSameImgImportedForProductWithNoDuplicates(String response) {
		JSONArray responseJson = new JSONArray(response);
		assertEquals(
				"import 1 images"
				, 1
				, responseJson.length());				
		
		assertEquals( 1L, imgRepo.count());		
		
		IntStream.range(0, responseJson.length())
				.mapToObj(responseJson::getJSONObject)
				.forEach(this::assertImageUploaded);
		
		List<String> urls = 
				IntStream.range(0, responseJson.length())
						.mapToObj(responseJson::getJSONObject)
						.map(obj -> obj.getString("image_url"))
						.distinct()
						.collect(toList());
		assertEquals("a 2 duplicate images was imported for variants of the same product, the duplicates shouldn't be imported if"
					+ " it is a product image"
						, 1, urls.size());
	}
	
	
	
	
	
	private void assertImgsImported(String response) {
		JSONArray responseJson = new JSONArray(response);
		assertEquals(
				"import 2 images for two variants"
				, 2
				, responseJson.length());				
		
		assertEquals( 2L, imgRepo.count());		
		
		IntStream.range(0, responseJson.length())
				.mapToObj(responseJson::getJSONObject)
				.forEach(this::assertImageUploaded);
	}
	
	
	
	
	private void assertImgsImported(String response, Long expectedCount) {
		JSONArray responseJson = new JSONArray(response);
		assertEquals(
				"import 2 images for two variants"
				, 2
				, responseJson.length());				
		
		assertEquals( expectedCount, imgRepo.countByProductEntity_OrganizationId(99001));
		
		IntStream.range(0, responseJson.length())
				.mapToObj(responseJson::getJSONObject)
				.forEach(this::assertImageUploaded);
	}
	
	
	
	
	private void assertImageUploaded(JSONObject response) {
		Long imgId = response.getLong("image_id");
		String imgUrl = response.getString("image_url");
		
		FileEntity fileEntity = filesRepo.findByUrl(imgUrl);
		Path imgPath = basePath.resolve(fileEntity.getLocation());
		
		assertTrue(imgRepo.existsById(imgId));		
		assertTrue(Files.exists(imgPath));
	}
	
	
	
	
	
	private JSONObject createDummyUploadRequest() {
		JSONObject metaData = new JSONObject();		
		
		metaData.put("type", 7);
		metaData.put("priority", 1);
		metaData.put("ignore_errors", false);
		
		return metaData;
	}
	
	
	
	
	private ResultActions performFileUpload(String csvFileName, byte[] json, String userToken) throws IOException, Exception {
		
		MockMultipartFile csvPart = createCsvPart(csvFileName);				
		MockPart jsonPart = createJsonPart(json);
		
		ResultActions result = 
		    mockMvc.perform(MockMvcRequestBuilders
		    					.multipart(PRODUCT_IMG_BULK_URL)
				                 .file(csvPart)
				                 .part(jsonPart)
				                 .header(TOKEN_HEADER, userToken));
		return result;
	}
	
	
	
	
	private void assertNoImgsImported() throws IOException {
		assertEquals(0L, imgRepo.count());
		assertEquals(0L, filesRepo.count());
		try(Stream<Path> files = Files.walk(basePath)){
			Long cnt = files.filter(path -> !Files.isDirectory(path) ).count();
			assertEquals("no files should exist in the save directory", 0L, cnt.longValue());
		}
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


	private void assertImgsImportedAndCollectionImagesRemained(String response) {
		JSONArray responseJson = new JSONArray(response);
		assertEquals(
				"import 2 images for two variants"
				, 2
				, responseJson.length());

		assertEquals( 4L, imgRepo.count());

		IntStream.range(0, responseJson.length())
				.mapToObj(responseJson::getJSONObject)
				.forEach(this::assertImageUploaded);
	}
}
