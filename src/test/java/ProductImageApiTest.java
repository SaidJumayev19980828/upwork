import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.FilesRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.ProductImgDetailsDTO;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.FileEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductImagesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.security.AuthenticationFilter;
import com.nasnav.service.ProductImageService;
import com.nasnav.test.commons.TestCommons;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:database.properties")
@NotThreadSafe 
@ContextConfiguration(initializers = BaseDirInitialzer.class) //overrides the property "files.basepath" to use temp dir 
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) //creates a new context with new temp dir for each test method
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_API_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class ProductImageApiTest {
	
	private static final long TEST_PRODUCT_ID = 1001L;

	private static final String USER_TOKEN = "101112";

	private static final String USER_FROM_OTHER_ORG_TOKEN = "131415";
	

	private static final String TEST_PHOTO = "nasnav--Test_Photo.png";
	private static final String TEST_PHOTO_UPDATED = "nasnav--Test_Photo_UPDATED.png";

	private static final String TEST_IMG_DIR = "src/test/resources/test_imgs_to_upload";

	private static final String EXPECTED_COVER_IMG_URL = "99001/cover_img.jpg";

	@Value("${files.basepath}")
	private String basePathStr;

	private Path basePath;
	
	
	@Autowired
	private ProductRepository productRepository;
	
	
	@Autowired
	private FilesRepository filesRepo;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	
	@Autowired
	private ProductImagesRepository imgRepo;
	
	
	
	@Autowired
	private  MockMvc mockMvc;
	
	
	
	@Autowired
	private ProductImageService imgService;
	
	
	@Autowired
	private TestRestTemplate template;

	
	@Autowired 
	private EmployeeUserRepository empUserRepo;
	
	
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
	public void productImageMissingImageIdIdTest() throws IOException, Exception {
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(1001L).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		imgReq.put("operation", Operation.UPDATE.getValue());
		imgReq.remove("image_id");
		
		uploadInvalidTestImg(fileName, imgReq.toString().getBytes() , product);
	}
	
	
	
	
	@Test
	public void productNewImageMissingImageIdIdTest() throws IOException, Exception {
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(1001L).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		imgReq.remove("image_id");
		
		uploadValidTestImg(fileName, imgReq.toString().getBytes() , product, imgReq);
	}
	
	
	
	
	@Test
	public void productImageUpdateNoImgTest() throws IOException, Exception {
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(1001L).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		imgReq.put("operation", Operation.UPDATE.getValue());
		imgReq.remove("image_id");
		
		Long orgId = product.getOrganizationId();		
		String sanitizedFileName = StringUtils.getFileNameSanitized(fileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		Path expectedPath = Paths.get(""+orgId).resolve(sanitizedFileName);
		
		performEmptyFileUpload(fileName, imgReq.toString().getBytes())
             .andExpect(status().is(406));
		
		 FileEntity file = filesRepo.findByUrl(expectedUrl);
		 Optional<ProductImagesEntity> img = imgRepo.findByUri(expectedUrl);
		 
		 assertFalse("Image File was not saved", Files.exists(expectedPath));
		 assertNull("No File record added to table FILES", file);
		 assertFalse("No image record added to table PRODUCT_IMAGES", img.isPresent());
	}
	
	
	
	
	
	@Test
	public void productImageMissingProductIdTest() throws IOException, Exception {
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(1001L).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		imgReq.remove("product_id");
		
		uploadInvalidTestImg(fileName, imgReq.toString().getBytes() , product);
	}
	
	
	
	
	
	
	
	@Test
	public void productImageNullProductIdTest() throws IOException, Exception {
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(1001L).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		imgReq.put("product_id", JSONObject.NULL);
		
		uploadInvalidTestImg(fileName, imgReq.toString().getBytes() , product);
	}
	
	
	
	
	@Test
	public void productImageNonExistingProductIdTest() throws IOException, Exception {
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(1001L).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		imgReq.put("product_id", 999999L);
		
		uploadInvalidTestImg(fileName, imgReq.toString().getBytes() , product);
	}
	
	
	
	
	
	@Test
	public void productImageProductIdFromAnotherOrgTest() throws IOException, Exception {
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(1001L).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		
		Long orgId = product.getOrganizationId();		
		String sanitizedFileName = StringUtils.getFileNameSanitized(fileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		Path expectedPath = Paths.get(""+orgId) .resolve(sanitizedFileName);
		
		performFileUploadForOtherOrg(fileName, imgReq.toString().getBytes())
             .andExpect(status().is(403));
		
		 FileEntity file = filesRepo.findByUrl(expectedUrl);
		 Optional<ProductImagesEntity> img = imgRepo.findByUri(expectedUrl);
		 
		 assertFalse("Image File was not saved", Files.exists(expectedPath));
		 assertNull("No File record added to table FILES", file);
		 assertFalse("No image record added to table PRODUCT_IMAGES", img.isPresent());
	}
	
	
	
	
	@Test
	public void productImageMissingOperationTest() throws IOException, Exception {
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(1001L).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		imgReq.remove("operation");
		
		uploadInvalidTestImg(fileName, imgReq.toString().getBytes() , product);
	}
	
	
	
	
	private void uploadInvalidTestImg(String fileName, byte[] json, ProductEntity product)
			throws Exception, IOException {
		
		Long orgId = product.getOrganizationId();		
		String sanitizedFileName = StringUtils.getFileNameSanitized(fileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		Path expectedPath = Paths.get(""+orgId).resolve(sanitizedFileName);
		
		@SuppressWarnings("unused")
		String result = 
			performFileUpload(fileName, json)
             .andExpect(status().is(406))
             .andReturn()
             .getResponse()
             .getContentAsString();
		
		 FileEntity file = filesRepo.findByUrl(expectedUrl);
		 Optional<ProductImagesEntity> img = imgRepo.findByUri(expectedUrl);
		 
		 assertFalse("Image File was not saved", Files.exists(expectedPath));
		 assertNull("No File record added to table FILES", file);
		 assertFalse("No image record added to table PRODUCT_IMAGES", img.isPresent());
	}
	
	
	
	
	@Test
	public void productNewImageUploadTest() throws Exception {
		
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(1001L).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		
		uploadValidTestImg(fileName, imgReq.toString().getBytes() , product, imgReq);		 
	}
	
	
	
	
	
	private JSONObject createDummyProductUploadImgRequest(ProductEntity product) {
		JSONObject imgData = new JSONObject();
		
		imgData.put("product_id", product.getId());
		imgData.put("operation", Operation.CREATE.getValue());
		imgData.put("type", 7);
		imgData.put("priority", 2);
		
		return imgData;
	}




	private JSONObject uploadValidTestImg(String fileName, byte[] json, ProductEntity product, JSONObject imgMetaData)
			throws Exception, IOException {
		
		Long orgId = product.getOrganizationId();		
		String sanitizedFileName = StringUtils.getFileNameSanitized(fileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		Path expectedPath = Paths.get(""+orgId).resolve(sanitizedFileName);
		
		Path saveDir = basePath.resolve(orgId.toString());
		
				
		String response = performFileUpload(fileName, json)
					             .andExpect(status().is(200))					            
					             .andReturn()
					             .getResponse()
					             .getContentAsString();
		
		JSONObject responseJson = new JSONObject(response);
		 
		 assertTrue("Ogranization directory was created", Files.exists(saveDir));
		 
		 try(Stream<Path> files = Files.list(saveDir)){
			 assertNotEquals("new Files exists in the expected location", 0L, files.count() );
		 }
		 
		 assertFileSavedToDb(fileName, orgId, expectedUrl, expectedPath);
		 
		 assertImgDataSavedToDb(imgMetaData, responseJson, expectedUrl);
		 
		 return responseJson;
	}
	
	
	
	
	
	private void assertImgDataSavedToDb(JSONObject imgMetaData, JSONObject responseJson, String expectedUrl) {
		
		assertEquals(expectedUrl, responseJson.get("image_url"));
		
		Long imgId = responseJson.getLong("image_id");
		assertNotNull(imgId);
		
		Optional<ProductImagesEntity> imgEntityOpt = imgRepo.findById(imgId);
		assertTrue(imgEntityOpt.isPresent());
		
		ProductImagesEntity imgEntity = imgEntityOpt.get();
		assertEquals(expectedUrl, imgEntity.getUri());
		assertEquals(responseJson.get("image_url"), imgEntity.getUri());
		
		assertEquals(imgMetaData.get("product_id"), imgEntity.getProductEntity().getId());
		
		Supplier<?> variantIdGetter = () -> Optional.ofNullable(imgEntity.getProductVariantsEntity())
												.map(ProductVariantsEntity::getId)
												.orElse(null);
		
		assertOptionalFieldSaved("variant_id", imgMetaData, variantIdGetter);
		assertOptionalFieldSaved("priority", imgMetaData, imgEntity::getPriority );
		assertOptionalFieldSaved("type", imgMetaData, imgEntity::getType );
	}





	private void assertOptionalFieldSaved(String fieldName, JSONObject imgMetaData, Supplier<?> getter) {
		if(fieldName == null)
			assertNull(getter.get());
		else
		Optional.ofNullable(fieldName)
				.filter(imgMetaData.keySet()::contains)
				.map(field -> imgMetaData.get(field))
				.ifPresent(val -> assertEquals(val, getter.get()) );
						
	}





	private ResultActions performFileUpload(String fileName, byte[] json) throws IOException, Exception {
		String testImgDir = TEST_IMG_DIR;
		Path img = Paths.get(testImgDir).resolve(fileName).toAbsolutePath();
		
		assertTrue(Files.exists(img));		
		
		byte[] imgData = Files.readAllBytes(img);		
		MockMultipartFile filePart = new MockMultipartFile("image", fileName, "image/png", imgData);
				
		MockPart jsonPart = new MockPart("properties", "properties",  json);		
		jsonPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		
		 ResultActions result = 
		    mockMvc.perform(MockMvcRequestBuilders.multipart("/product/image")
								                 .file(filePart)
								                 .part(jsonPart)
								                 .header(AuthenticationFilter.TOKEN_HEADER, USER_TOKEN));
		return result;
	}
	
	
	
	
	private ResultActions performFileUploadForOtherOrg(String fileName, byte[] json) throws IOException, Exception {
		String testImgDir = TEST_IMG_DIR;
		Path img = Paths.get(testImgDir).resolve(fileName).toAbsolutePath();
		
		assertTrue(Files.exists(img));		
		
		byte[] imgData = Files.readAllBytes(img);		
		MockMultipartFile filePart = new MockMultipartFile("image", fileName, "image/png", imgData);
				
		MockPart jsonPart = new MockPart("properties", "properties",  json);		
		jsonPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		
		 ResultActions result = 
		    mockMvc.perform(MockMvcRequestBuilders.multipart("/product/image")
								                 .file(filePart)
								                 .part(jsonPart)
								                 .header(AuthenticationFilter.TOKEN_HEADER, USER_FROM_OTHER_ORG_TOKEN));
		return result;
	}
	
	
	
	
	
	
	private ResultActions performEmptyFileUpload(String fileName, byte[] json) throws IOException, Exception {
		String testImgDir = TEST_IMG_DIR;
		Path img = Paths.get(testImgDir).resolve(fileName).toAbsolutePath();
		
		assertTrue(Files.exists(img));		
		
		byte[] imgData = new byte[0];		
		MockMultipartFile filePart = new MockMultipartFile("image", fileName, "image/png", imgData);
				
		MockPart jsonPart = new MockPart("properties", "properties",  json);		
		jsonPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		
		 ResultActions result = 
		    mockMvc.perform(MockMvcRequestBuilders.multipart("/product/image")
								                 .file(filePart)
								                 .part(jsonPart)
								                 .header(AuthenticationFilter.TOKEN_HEADER, USER_TOKEN));
		return result;
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
	public void productUpdateImageUploadTest() throws Exception {
		//create new image 
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(TEST_PRODUCT_ID).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		
		JSONObject origResponse = uploadValidTestImg(fileName, imgReq.toString().getBytes() , product, imgReq);	
		
		//================================
				
		String updatedFileName = TEST_PHOTO_UPDATED;

		String origImgUrl = origResponse.getString("image_url");
		FileEntity origImgFileEntity = filesRepo.findByUrl(origImgUrl);
		ProductImagesEntity origImgEntity = imgRepo.findById( origResponse.getLong("image_id") ).get();
		
		JSONObject updateReq = new JSONObject();
		updateReq.put("operation", Operation.UPDATE.getValue());
		updateReq.put("image_id",  origResponse.getLong("image_id"));
		updateReq.put("product_id", TEST_PRODUCT_ID);
		updateReq.put("priority", 999);
				
		String response = performFileUpload(updatedFileName, updateReq.toString().getBytes())
					             .andExpect(status().is(200))					            
					             .andReturn()
					             .getResponse()
					             .getContentAsString();
		
		
		validateUpdatedImg(product, origResponse, updateReq, response, origImgFileEntity, origImgEntity);
		
	}





	private void validateUpdatedImg(ProductEntity product, JSONObject origImgresponseJson,
			JSONObject updateReq, String response , FileEntity origImgFileEntity , ProductImagesEntity origImgEntity) {
		String updatedFileName = TEST_PHOTO_UPDATED;
		
		Long imgId = origImgresponseJson.getLong("image_id");
		
		Path origImgPath = Paths.get(origImgFileEntity.getLocation());
		
		Long orgId = product.getOrganizationId();
		String sanitizedFileName = StringUtils.getFileNameSanitized(updatedFileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		Path expectedPath = Paths.get(""+orgId).resolve(sanitizedFileName);
		
		
		JSONObject responseJson = new JSONObject(response);
		Long updatedImgId = responseJson.getLong("image_id");
		ProductImagesEntity updatedImgEntity = imgRepo.findById(updatedImgId).get();
		
		assertEquals(imgId, updatedImgId) ;
		assertTrue( Files.exists( basePath.resolve(expectedPath) ));
		assertFalse( Files.exists( basePath.resolve(origImgPath) ) );
		
		assertFalse(filesRepo.existsById(origImgFileEntity.getId()));
		assertTrue(imgRepo.existsById(imgId));
		
		assertFileSavedToDb(updatedFileName, orgId, expectedUrl, expectedPath);
		
		//non changed properties
		assertEquals(origImgEntity.getType(), updatedImgEntity.getType());
		assertEquals(origImgEntity.getProductEntity(), updatedImgEntity.getProductEntity());
		assertEquals(origImgEntity.getProductVariantsEntity(), updatedImgEntity.getProductVariantsEntity());
		
		//changed properties
		assertEquals(responseJson.get("image_url"), updatedImgEntity.getUri());
		assertEquals(expectedUrl, updatedImgEntity.getUri());
		assertEquals(updateReq.getInt("priority"), updatedImgEntity.getPriority().intValue());
	}
	
	
	
	
	@Test
	public void productUpdateImageChangeProductTest() throws Exception {
		//create new image 
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(TEST_PRODUCT_ID).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		
		JSONObject origResponse = uploadValidTestImg(fileName, imgReq.toString().getBytes() , product, imgReq);	
		
		//================================
				
		String updatedFileName = TEST_PHOTO_UPDATED;

		String origImgUrl = origResponse.getString("image_url");
		FileEntity origImgFileEntity = filesRepo.findByUrl(origImgUrl);
		ProductImagesEntity origImgEntity = imgRepo.findById( origResponse.getLong("image_id") ).get();
		
		JSONObject updateReq = new JSONObject();
		updateReq.put("operation", Operation.UPDATE.getValue());
		updateReq.put("image_id",  origResponse.getLong("image_id"));
		updateReq.put("product_id", 1003L); //original image was for product_id = TEST_PRODUCT_ID
				
		String response = performFileUpload(updatedFileName, updateReq.toString().getBytes())
					             .andExpect(status().is(200))					            
					             .andReturn()
					             .getResponse()
					             .getContentAsString();
		
		
		validateUpdatedImgWithNewProduct(product, origResponse, updateReq, response, origImgFileEntity, origImgEntity);
	}
	
	
	
	private void validateUpdatedImgWithNewProduct(ProductEntity product, JSONObject origImgresponseJson,
			JSONObject updateReq, String response , FileEntity origImgFileEntity , ProductImagesEntity origImgEntity) {
		String updatedFileName = TEST_PHOTO_UPDATED;
		
		Long imgId = origImgresponseJson.getLong("image_id");
		
		Path origImgPath = Paths.get(origImgFileEntity.getLocation());
		
		Long orgId = product.getOrganizationId();
		String sanitizedFileName = StringUtils.getFileNameSanitized(updatedFileName);		
		String expectedUrl = orgId + "/" + sanitizedFileName;
		Path expectedPath = Paths.get(""+orgId).resolve(sanitizedFileName);
		
		
		JSONObject responseJson = new JSONObject(response);
		Long updatedImgId = responseJson.getLong("image_id");
		ProductImagesEntity updatedImgEntity = imgRepo.findById(updatedImgId).get();
		
		assertEquals(imgId, updatedImgId) ;
		assertTrue( Files.exists( basePath.resolve(expectedPath) ));
		assertFalse( Files.exists( basePath.resolve(origImgPath) ) );
		
		assertFalse(filesRepo.existsById(origImgFileEntity.getId()));
		assertTrue(imgRepo.existsById(imgId));
		
		assertFileSavedToDb(updatedFileName, orgId, expectedUrl, expectedPath);
		
		//non changed properties
		assertEquals(origImgEntity.getType(), updatedImgEntity.getType());		
		assertEquals(origImgEntity.getProductVariantsEntity(), updatedImgEntity.getProductVariantsEntity());
		assertEquals(origImgEntity.getPriority(), updatedImgEntity.getPriority());
		
		//changed properties
		assertNotEquals(origImgEntity.getProductEntity(), updatedImgEntity.getProductEntity());
		assertEquals(responseJson.get("image_url"), updatedImgEntity.getUri());
		assertEquals(expectedUrl, updatedImgEntity.getUri());
		
	}
	
	
	
	@Test
	public void productUpdateImageFailedTest() throws Exception {
		//create new image 
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(TEST_PRODUCT_ID).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		
		JSONObject origResponse = uploadValidTestImg(fileName, imgReq.toString().getBytes() , product, imgReq);	
		
		//================================
				
		String updatedFileName = TEST_PHOTO_UPDATED;
		
		//original img data
		String origImgUrl = origResponse.getString("image_url");
		FileEntity origImgFileEntity = filesRepo.findByUrl(origImgUrl);
		ProductImagesEntity origImg = imgRepo.findById( origResponse.getLong("image_id") ).get();
		Path origImgPath = Paths.get(origImgFileEntity.getLocation());
		
		//update image - should fail
		JSONObject updateReq = new JSONObject();
		updateReq.put("operation", Operation.UPDATE.getValue());
		updateReq.remove("image_id");
		updateReq.put("product_id", TEST_PRODUCT_ID);
		updateReq.put("priority", 999);
				
		performFileUpload(updatedFileName, updateReq.toString().getBytes())
        			.andExpect(status().is(406)); 
		
		
		//check if image was actually updated , and if original data still exists
		ProductImagesEntity updatedImg = imgRepo.findById( origResponse.getLong("image_id") ).get();		
		
		Long orgId = product.getOrganizationId();
		String sanitizedFileName = StringUtils.getFileNameSanitized(updatedFileName);		
		Path expectedPath = Paths.get(""+orgId).resolve(sanitizedFileName);
		
		
		assertEquals("img shouldn't be updated",origImg ,updatedImg );
		assertTrue(filesRepo.existsById( origImgFileEntity.getId() ));
		assertFalse( Files.exists( basePath.resolve(expectedPath) ));
		assertTrue( Files.exists( basePath.resolve(origImgPath) ) );
	}
	
	
	
	
	@Test
	public void productImageDeleteTest() throws Exception {
		//create new image 
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(TEST_PRODUCT_ID).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		
		JSONObject origResponse = uploadValidTestImg(fileName, imgReq.toString().getBytes() , product, imgReq);	
		
		//================================			
		//delete the image
		
		String origImgUrl = origResponse.getString("image_url");
		Long origImgId = origResponse.getLong("image_id");
		FileEntity origImgFileEntity = filesRepo.findByUrl(origImgUrl);
		Path filePath = basePath.resolve(origImgFileEntity.getLocation());
		ProductImagesEntity origImgEntity = imgRepo.findById( origResponse.getLong("image_id") ).get();
		
		
		ResultActions result = 
			    mockMvc.perform(MockMvcRequestBuilders.delete("/product/image")
			    									 .param("image_id", origImgId.toString())
									                 .header(AuthenticationFilter.TOKEN_HEADER, USER_TOKEN));		
		String response = result.andExpect(status().is(200))					            
					             .andReturn()
					             .getResponse()
					             .getContentAsString();
		
		
		JSONObject deleteResponse = new JSONObject(response);
		
		assertEquals(deleteResponse.getLong("product_id"), origImgEntity.getProductEntity().getId().longValue() );
		assertFalse(imgRepo.existsById( origImgId ));
		assertFalse(filesRepo.existsById( origImgFileEntity.getId() ));
		assertFalse(Files.exists( filePath ));
	}
	
	
	
	@Test
	public void productImageDeleteFailTest() throws Exception {
		//create new image 
		String fileName = TEST_PHOTO;
		ProductEntity product = productRepository.findById(TEST_PRODUCT_ID).get();		
		
		JSONObject imgReq = createDummyProductUploadImgRequest(product);
		
		JSONObject origResponse = uploadValidTestImg(fileName, imgReq.toString().getBytes() , product, imgReq);	
		
		//================================			
		//try to delete the image
		
		String origImgUrl = origResponse.getString("image_url");
		Long origImgId = origResponse.getLong("image_id");
		FileEntity origImgFileEntity = filesRepo.findByUrl(origImgUrl);
		Path filePath = basePath.resolve(origImgFileEntity.getLocation());		
		
		
	    mockMvc.perform(delete("/product/image")
							 .param("image_id", "9999999")
			                 .header(AuthenticationFilter.TOKEN_HEADER, USER_TOKEN))
	    		.andExpect(status().is(406));		
		
		
		assertTrue(imgRepo.existsById( origImgId ));
		assertTrue(filesRepo.existsById( origImgFileEntity.getId() ));
		assertTrue(Files.exists( filePath ));
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_Cover_Image_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getProductCoverImageTest() {
		assertEquals(EXPECTED_COVER_IMG_URL, imgService.getProductCoverImage(1001L));
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_API_Test_Data_Insert_2.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getProductImagesTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);
		Long productId = 1008L;
		Set<ProductImgDetailsDTO> expectedData = getExpectedProductImgs(productId);
		
		HttpEntity<?> request =  getHttpEntity("" , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/product/images?product_id=" + productId
						, HttpMethod.GET
						, request
						, String.class);
		
		ObjectMapper mapper = new ObjectMapper();
		Set<ProductImgDetailsDTO> fetched = mapper.readValue(response.getBody(), new TypeReference<Set<ProductImgDetailsDTO>>(){});
		
		assertEquals(3L, fetched.size());
		assertEquals(expectedData, fetched);
	}





	private Set<ProductImgDetailsDTO> getExpectedProductImgs(Long productId) {
		Set<ProductImgDetailsDTO> expected = new HashSet<>();
		
		ProductImgDetailsDTO img1 = new ProductImgDetailsDTO(45001L, productId, null, "cool_img.png", 7, 0);
		ProductImgDetailsDTO img2 = new ProductImgDetailsDTO(45002L, productId, 310008L, "cool_img.png", 7, 0);
		ProductImgDetailsDTO img3 = new ProductImgDetailsDTO(45003L, null	  , 310008L, "cool_img.png", 7, 0);
		expected.add(img1);
		expected.add(img2);
		expected.add(img3);
		
		return expected;
	}
	
	
	
	
	
	@Test
	public void getProductImagesNoAuthZTest() throws JsonParseException, JsonMappingException, IOException {
		Long productId = 1008L;
		
		HttpEntity<?> request =  TestCommons.getHttpEntity("" , "NOT-EXISTING_TOKEN");
		
		ResponseEntity<String> response = 
				template.exchange("/product/images?product_id=" + productId
						, HttpMethod.GET
						, request
						, String.class);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void getProductImagesNoAuthNTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(70L);
		Long productId = 1008L;
		
		HttpEntity<?> request =  TestCommons.getHttpEntity("" , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/product/images?product_id=" + productId
						, HttpMethod.GET
						, request
						, String.class);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void getProductImagesAnotherOrgAdminTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(68L);
		Long productId = 1008L;
		
		HttpEntity<?> request =  TestCommons.getHttpEntity("" , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/product/images?product_id=" + productId
						, HttpMethod.GET
						, request
						, String.class);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void getProductImagesNonExistingProductTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(68L);
		Long productId = 105487845408L;
		
		HttpEntity<?> request =  getHttpEntity("" , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/product/images?product_id=" + productId
						, GET
						, request
						, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_Cover_Image_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getProductsCoverImgsTest() {
		List<Long> productIds = Arrays.asList(1001L, 1002L, 1003L);
		Map<Long, String> coverImgs = imgService.getProductsCoverImages(productIds);
		Set<String> expectedUris = new HashSet<>( Arrays.asList("99001/cover_img.jpg", "99001/cover_img3.jpg", "no_img_found.jpg"));
		
		assertEquals("Only products 1001, 1002 have images, but 1003 should have the fallback image url", 3, coverImgs.keySet().size());
		assertEquals("expected cover images uri's" , expectedUris, new HashSet<>(coverImgs.values()) );
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void deleteAllImagesNotConfirmed() {
		Long countBefore = imgRepo.countByProductEntity_OrganizationId(99001L);
		assertNotEquals(0L,  countBefore.longValue());
		
		HttpEntity<?> request =  getHttpEntity("" , "101112");
		ResponseEntity<String> response = 
				template.exchange("/product/image/all?confirmed=false"
						, DELETE
						, request
						, String.class);
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
		Long countAfter = imgRepo.countByProductEntity_OrganizationId(99001L);
		assertEquals(countBefore,  countAfter);
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void deleteAllImages() {
		Long countBefore = imgRepo.countByProductEntity_OrganizationId(99001L);
		assertNotEquals(0L,  countBefore.longValue());
		
		HttpEntity<?> request =  getHttpEntity("" , "101112");
		ResponseEntity<String> response = 
				template.exchange("/product/image/all?confirmed=true"
						, DELETE
						, request
						, String.class);
		
		assertEquals(OK, response.getStatusCode());
		Long countAfter = imgRepo.countByProductEntity_OrganizationId(99001L);
		assertEquals(0L,  countAfter.longValue());
	}
	
	
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void deleteAllImagesNoAuthz() {
		Long countBefore = imgRepo.countByProductEntity_OrganizationId(99001L);
		assertNotEquals(0L,  countBefore.longValue());
		
		HttpEntity<?> request =  getHttpEntity("" , "NO_AUTH");
		ResponseEntity<String> response = 
				template.exchange("/product/image/all?confirmed=true"
						, DELETE
						, request
						, String.class);
		
		assertEquals(UNAUTHORIZED, response.getStatusCode());
		Long countAfter = imgRepo.countByProductEntity_OrganizationId(99001L);
		assertEquals(countBefore,  countAfter);
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void deleteAllImagesNoAuthN() {
		Long countBefore = imgRepo.countByProductEntity_OrganizationId(99001L);
		assertNotEquals(0L,  countBefore.longValue());
		
		HttpEntity<?> request =  getHttpEntity("" , "erereres");
		ResponseEntity<String> response = 
				template.exchange("/product/image/all?confirmed=true"
						, DELETE
						, request
						, String.class);
		
		assertEquals(FORBIDDEN, response.getStatusCode());
		Long countAfter = imgRepo.countByProductEntity_OrganizationId(99001L);
		assertEquals(countBefore,  countAfter);
	}
}
