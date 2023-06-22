package com.nasnav.test;

import com.nasnav.dao.FilesRepository;
import com.nasnav.dao.ProductExtraAttributesEntityRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.persistence.FileEntity;
import com.nasnav.persistence.ProductExtraAttributesEntity;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@Sql(executionPhase= BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert.sql"})
@Sql(executionPhase= AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class ProductImageBulkUploadTest extends AbstractTestWithTempBaseDir {
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
	
	private static final String TEST_ZIP_UPLOADED_WITH_CSV = "img_bulk_upload_with_csv.zip";
	
	private static final String TEST_ZIP_MULTI_BARCODE_SAME_FILE = "img_bulk_upload_multi_barcode_same_file.zip";

    private static final String TEST_CSV_VARIANT_ID_EXISTING_VARIANT = "img_bulk_exist_variant_id_exist_variant.csv";

	private static final String TEST_CSV_VARIANT_ID_NO_VARIANT = "img_bulk_exist_variant_id_no_variant.csv";

	private static final String TEST_ZIP_WITH_VARIANTS = "img_bulk_upload_with_variants.zip";

	private static final String TEST_CSV_EXTERNAL_ID_EXISTING_MAPPING = "img_bulk_exist_external_id_exist_mapping.csv";

	private static final String TEST_CSV_EXTERNAL_ID_NO_MAPPING = "img_bulk_exist_external_id_no_mapping.csv";

	private static final String TEST_CSV_MISSING_PATH = "img_bulk_barcode_missing_path.csv";
	
	private static final String TEST_ZIP_UPLOADED_WITH_CSV_MISSING_PATH = "img_bulk_upload_with_csv_missing_path.zip";

	private static final String TEST_CSV_OTHER_ORG_VARIANTS = "img_bulk_url_variant_from_other_org.csv";




	@Autowired
	private FilesRepository filesRepo;
	
	@Autowired
	private ProductImagesRepository imgRepo;
	
	@Autowired
	private  MockMvc mockMvc;

	@Autowired
	private ProductExtraAttributesEntityRepository extraAttrRepo;


	@Test
	public void updateImgBulkNoAuthz() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_ZIP, TEST_CSV, jsonBytes, "NON-EXISTING-TOKEN")
	             .andExpect(status().is(401));
		
		assertNoImgsImported();
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkNoAuthN() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_ZIP, TEST_CSV, jsonBytes, STORE_ADMIN_TOKEN)
	             .andExpect(status().is(403));
		
		assertNoImgsImported();
	}
	
	
	
	
	@Test
	public void updateImgBulkNoZip() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		MockMultipartFile csvPart = createCsvPart(TEST_CSV);				
		MockPart jsonPart = createJsonPart(jsonBytes);
		
	    mockMvc.perform(MockMvcRequestBuilders.multipart(PRODUCT_IMG_BULK_URL)
							                 .file(csvPart)
							                 .part(jsonPart)
							                 .header(TOKEN_HEADER, USER_TOKEN))
	    		.andExpect(status().is(400));
	    
	    assertNoImgsImported();
	}
	
	
	
	
	
	@Test
	public void updateImgBulkMissingType() throws IOException, Exception {
		
		JSONObject json = createDummyUploadRequest();
		json.remove("type");
		byte[] jsonBytes = json.toString().getBytes();
		
		performFileUpload(TEST_ZIP, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(406));
		
		assertNoImgsImported();
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkMissingPriority() throws IOException, Exception {
		
		JSONObject json = createDummyUploadRequest();
		json.remove("priority");
		
		byte[] jsonBytes = json.toString().getBytes();
		
		performFileUpload(TEST_ZIP, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(406));	
		
		assertNoImgsImported();
	}
	
	
	
	
	@Test
	public void updateImgBulkEmptyZip() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		MockMultipartFile zipPart = createEmptyZipPart(TEST_ZIP);	
		MockMultipartFile csvPart = createCsvPart(TEST_CSV);				
		MockPart jsonPart = createJsonPart(jsonBytes);
		
	    mockMvc.perform(MockMvcRequestBuilders.multipart(PRODUCT_IMG_BULK_URL)
	    									 .file(zipPart)
							                 .file(csvPart)
							                 .part(jsonPart)
							                 .header(TOKEN_HEADER, USER_TOKEN))
	    		.andExpect(status().is(406));
	    
	    assertNoImgsImported();
	}
	
	
	
	
	@Test
	public void updateImgBulkNotZipFile() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_CSV, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(406));
		
		assertNoImgsImported();
	}
	
	
	
	
	@Test
	public void updateImgBulkInvalidZipFile() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		//if the zip file is invalid , nothing will be read from it , but
		//no exception is thrown.
		performFileUpload(TEST_ZIP_INVALID, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200));		
		
		assertNoImgsImported();
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkTestInvalidBarcode() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();

		performFileUpload(TEST_ZIP_NON_EXISTING_BARCODE, TEST_CSV_NON_EXISTING_BARCODE, jsonBytes, USER_TOKEN)
		 .andExpect(status().is(500))
		 .andReturn()
		 .getResponse()
		 .getContentAsString();

		assertNoImgsImported();
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkAdminOfOtherOrg() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_ZIP, TEST_CSV, jsonBytes, OTHER_ORG_ADMIN_TOKEN)
		 .andExpect(status().is(500))
		 .andReturn()
		 .getResponse()
		 .getContentAsString();

		assertNoImgsImported();
	}
	
	
	
	
	
	@Test
	public void updateImgBulkWithCSVTest() throws IOException, Exception {
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_ZIP_UPLOADED_WITH_CSV, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		assertImgsImported(response);
	}




	@Test
	@Sql(executionPhase= BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert_5.sql"})
	@Sql(executionPhase= AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void updateImgBulkWithCSVAndDeleteOldImagesTest() throws IOException, Exception {
		boolean oldCollectionImgExists = imgRepo.existsById(66601L);
		boolean oldProductImgExists = imgRepo.existsById(66602L);
		assertTrue(oldProductImgExists);
		assertTrue(oldCollectionImgExists);

		byte[] jsonBytes =
				createDummyUploadRequest()
				.put("delete_old_images", true)
				.toString()
				.getBytes();

		String response =
				performFileUpload(TEST_ZIP_UPLOADED_WITH_CSV, TEST_CSV, jsonBytes, USER_TOKEN)
						.andExpect(status().is(200))
						.andReturn()
						.getResponse()
						.getContentAsString();

		assertImgsImportedAndCollectionImagesRemained(response);

		boolean oldCollectionImgExistsAfter = imgRepo.existsById(66601L);
		boolean oldProductImgExistsAfter = imgRepo.existsById(66602L);
		assertTrue(oldProductImgExistsAfter); // product images wasn't updated so old images shouldn't be removed
		assertTrue(oldCollectionImgExistsAfter);
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkWithCSVAndMultipleBarcodeForSamePathTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_ZIP_MULTI_BARCODE_SAME_FILE, TEST_CSV_MULTI_BARCODE_PER_PATH, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		assertSameImgImportedForProductAndVariants(response);
	}
	
	
	
	
	
	@Test
	public void updateImgBulkWithCSVAndInternalStructureTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
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
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
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
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		String response = performFileUploadNoCSV(TEST_ZIP, jsonBytes, USER_TOKEN)
									.andExpect(status().is(200))
						    		.andReturn()
						            .getResponse()
						            .getContentAsString();	
		
		assertImgsImported(response);
	}



	@Test
	public void updateImgBulkWithIncompleteCSVTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
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
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_ZIP_EMPTY_IMG_FILE, TEST_CSV, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(500))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		JSONObject errorResponse = new JSONObject(response);
		JSONArray errors = errorResponse.getJSONArray("errors");
		
		assertTrue(errorResponse.has("errors"));
		assertEquals(1, errors.length());
		
		assertNoImgsImported();
	}


	@Test
	public void updateImgBulkTestExistVariantIdExistVariantEntity() throws IOException, Exception {
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();

		String response =
				performFileUpload(TEST_ZIP_WITH_VARIANTS , TEST_CSV_VARIANT_ID_EXISTING_VARIANT, jsonBytes, USER_TOKEN)
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
				performFileUpload(TEST_ZIP_WITH_VARIANTS , TEST_CSV_EXTERNAL_ID_EXISTING_MAPPING, jsonBytes, USER_TOKEN)
						.andExpect(status().is(200))
						.andReturn()
						.getResponse()
						.getContentAsString();

		assertImgsImported(response);
	}



	@Test
	public void updateImgBulkTestExistVariantIdNoVariant() throws Exception {
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();

		performFileUpload(TEST_ZIP_WITH_VARIANTS , TEST_CSV_VARIANT_ID_NO_VARIANT, jsonBytes, USER_TOKEN)
				.andExpect(status().is(500));

		assertNoImgsImported();
	}



	@Test
	public void updateImgBulkTestExistExternalIdNoMapping() throws Exception {
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();

		performFileUpload(TEST_ZIP_WITH_VARIANTS , TEST_CSV_EXTERNAL_ID_NO_MAPPING, jsonBytes, USER_TOKEN)
				.andExpect(status().is(500));

		assertNoImgsImported();
	}



	@Test
	public void updateImgBulkWithCSVWithMissingPathTest() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		String response = 
				performFileUpload(TEST_ZIP_UPLOADED_WITH_CSV_MISSING_PATH, TEST_CSV_MISSING_PATH, jsonBytes, USER_TOKEN)
	             .andExpect(status().is(200))
	             .andReturn()
	             .getResponse()
	             .getContentAsString();

		assertOneImgImported(response);
	}



	@Test
	public void updateSwatchImgBulkTest() throws IOException, Exception {

		byte[] jsonBytes = createDummySwatchUploadRequest().toString().getBytes();

		performFileUpload(TEST_ZIP_MULTI_BARCODE_SAME_FILE, TEST_CSV_MULTI_BARCODE_PER_PATH, jsonBytes, USER_TOKEN)
				.andExpect(status().is(200))
				.andReturn()
				.getResponse()
				.getContentAsString();

		assertSwatchImgsImported();
	}



	@Test
	public void updateSwatchImgBulkNonSwatchFeatureTest() throws IOException, Exception {

		JSONObject request =  createDummySwatchUploadRequest();
		request.put("feature_id", 235L);
		byte[] jsonBytes = request.toString().getBytes();

		performFileUpload(TEST_ZIP_MULTI_BARCODE_SAME_FILE, TEST_CSV_MULTI_BARCODE_PER_PATH, jsonBytes, USER_TOKEN)
				.andExpect(status().is(406));

		assertNoSwatchImgsImported();
	}


	@Test
	public void updateSwatchImgBulkFeatureFromOtherOrgTest() throws IOException, Exception {

		JSONObject request =  createDummySwatchUploadRequest();
		request.put("feature_id", 236L);
		byte[] jsonBytes = request.toString().getBytes();

		performFileUpload(TEST_ZIP_MULTI_BARCODE_SAME_FILE, TEST_CSV_MULTI_BARCODE_PER_PATH, jsonBytes, USER_TOKEN)
				.andExpect(status().is(406));

		assertNoSwatchImgsImported();
	}



	@Test
	public void updateSwatchImgBulkVariantNotExistsTest() throws IOException, Exception {

		JSONObject request =  createDummySwatchUploadRequest();
		byte[] jsonBytes = request.toString().getBytes();

		performFileUpload(TEST_ZIP_WITH_VARIANTS , TEST_CSV_VARIANT_ID_NO_VARIANT, jsonBytes, USER_TOKEN)
				.andExpect(status().is(200));

		//if the variant doesn't exists, it is simply ignored
		assertNoSwatchImgsImported();
	}



	@Test
	public void updateSwatchImgBulkVariantFromOtherOrgTest() throws IOException, Exception {
		JSONObject request =  createDummySwatchUploadRequest();
		byte[] jsonBytes = request.toString().getBytes();

		performFileUpload(TEST_ZIP_WITH_VARIANTS , TEST_CSV_OTHER_ORG_VARIANTS, jsonBytes, USER_TOKEN)
				.andExpect(status().is(406));

		//if the variant doesn't exists in the organization, it is simply ignored
		assertNoSwatchImgsImported();
	}



	@Test
	@Sql(executionPhase= BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert_4.sql"})
	@Sql(executionPhase= AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void updateExistingSwatchImgBulkTest() throws IOException, Exception {
		boolean hasOldValueBefore = variantHasOldSwatch(310001L, 310002L);
		assertTrue(hasOldValueBefore);

		byte[] jsonBytes = createDummySwatchUploadRequest().toString().getBytes();

		performFileUpload(TEST_ZIP_MULTI_BARCODE_SAME_FILE, TEST_CSV_MULTI_BARCODE_PER_PATH, jsonBytes, USER_TOKEN)
				.andExpect(status().is(200))
				.andReturn()
				.getResponse()
				.getContentAsString();

		assertSwatchImgsImported();

		boolean hasOldValueAfter = variantHasOldSwatch(310001L, 310002L);
		assertFalse(hasOldValueAfter);
	}



	private boolean variantHasOldSwatch(Long... variants) {
		return extraAttrRepo
				.findByExtraAttribute_NameAndVariantIdIn("$s-size$IMG_SWATCH", asList(variants))
				.stream()
				.allMatch(val -> val.getValue().equals("OLD_SWATCH"));
	}



	@Test
	@Sql(executionPhase= BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert_4.sql"})
	@Sql(executionPhase= AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void deleteOldSwatchesTest() throws IOException, Exception {
		boolean hasOldValueBefore = variantHasOldSwatch(310001L, 310002L, 310003L);
		assertTrue(hasOldValueBefore);

		JSONObject request =  createDummySwatchUploadRequest();
		request.put("delete_old_images", true);
		byte[] jsonBytes = request.toString().getBytes();

		performFileUpload(TEST_ZIP_MULTI_BARCODE_SAME_FILE, TEST_CSV_MULTI_BARCODE_PER_PATH, jsonBytes, USER_TOKEN)
				.andExpect(status().is(200))
				.andReturn()
				.getResponse()
				.getContentAsString();

		assertSwatchImgsImported();

		boolean hasOldValueAfter = variantHasOldSwatch(310001L, 310002L);
		assertFalse(hasOldValueAfter);

		boolean otherSwatchesDeleted =
				extraAttrRepo
				.findByExtraAttribute_NameAndVariantIdIn("$s-size$IMG_SWATCH", asList(310003L))
				.isEmpty();
		assertTrue(otherSwatchesDeleted);
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
	
	
	
	private void assertOneImgImported(String response) {
		JSONArray responseJson = new JSONArray(response);
		assertEquals(
				"import 1 images, images with no path are ignored"
				, 1
				, responseJson.length());				
		
		assertEquals( 1L, imgRepo.count());		
		
		IntStream.range(0, responseJson.length())
				.mapToObj(responseJson::getJSONObject)
				.forEach(this::assertImageUploaded);
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
		
		List<String> urls = IntStream.range(0, responseJson.length())
									.mapToObj(responseJson::getJSONObject)
									.map(obj -> obj.getString("image_url"))
									.distinct()
									.collect(toList());
		assertEquals("a single image was imported for the product and its variants, so they all should reference the same url"
						, 1, urls.size());
	}
	

	
	
	
	
	
	private void assertNoImgsImported() throws IOException {
		assertEquals(0L, imgRepo.count());
		assertEquals(0L, filesRepo.count());
		try(Stream<Path> files = Files.walk(basePath)){
			Long cnt = files.filter(path -> !Files.isDirectory(path) ).count();
			assertEquals("no files should exist in the save directory", 0L, cnt.longValue());
		}
	}



	private void assertSwatchImgsImported() {
		assertEquals( "Swatches are not saved as product images", 0L, imgRepo.count());

		List<ProductExtraAttributesEntity> swatchUrls =
				extraAttrRepo
				.findByExtraAttribute_NameAndVariantIdIn("$s-size$IMG_SWATCH", asList(310001L,310002L));
		assertEquals(2, swatchUrls.size());
		swatchUrls
			.stream()
			.map(ProductExtraAttributesEntity::getValue)
			.forEach(this::assertSwatchImageUploaded);
	}



	private void assertNoSwatchImgsImported() {
		assertEquals( "Swatches are not saved as product images", 0L, imgRepo.count());

		List<ProductExtraAttributesEntity> swatchUrls =
				extraAttrRepo
						.findByExtraAttribute_NameAndVariantIdIn("$s-size$IMG_SWATCH", asList(310001L,310002L));
		assertEquals(0, swatchUrls.size());
	}



	
	

	private ResultActions performFileUploadNoCSV(String zipFileName, byte[] json, String userToken)
			throws UnsupportedEncodingException, Exception {
		MockMultipartFile zipPart = createZipPart(zipFileName);		
		MockPart jsonPart = createJsonPart(json);
		
		return	mockMvc.perform(MockMvcRequestBuilders.multipart(PRODUCT_IMG_BULK_URL)
	    									 .file(zipPart)
							                 .part(jsonPart)
							                 .header(TOKEN_HEADER, USER_TOKEN))
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



	private void assertSwatchImageUploaded(String imgUrl) {
		FileEntity fileEntity = filesRepo.findByUrl(imgUrl);
		Path imgPath = basePath.resolve(fileEntity.getLocation());
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
								                 .header(TOKEN_HEADER, userToken));
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
	
	
	
	
	
	
	private JSONObject createDummyUploadRequest() {
		JSONObject metaData = new JSONObject();		
		
		metaData.put("type", 7);
		metaData.put("priority", 1);
		metaData.put("ignore_errors", false);
		
		return metaData;
	}



	private JSONObject createDummySwatchUploadRequest() {
		JSONObject metaData = new JSONObject();

		metaData.put("type", 7);
		metaData.put("priority", 1);
		metaData.put("ignore_errors", true);
		metaData.put("feature_id", 234);

		return metaData;
	}
}
