package com.nasnav.test;

import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dto.response.navbox.VariantsResponse;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.ProductExtraAttributesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.VariantFeatureValueEntity;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import com.nasnav.test.helpers.TestHelper;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.primitives.Longs.asList;
import static com.nasnav.constatnts.EntityConstants.Operation.UPDATE;
import static com.nasnav.enumerations.ExtraAttributeType.INVISIBLE;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Product_Variants_Test_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
class ProductVariantApiTest extends AbstractTestWithTempBaseDir {
	
	private static final Long TEST_VARIANT_ID = 80001L;
	private static final String TEST_IMG_DIR = "src/test/resources/test_imgs_to_upload";
	private static final String TEST_PHOTO = "nasnav--Test_Photo.png";


	private Long TEST_PRODUCT_ID = 1002L;

	@Autowired
	MockMvc mockMvc;


	@Autowired
	private TestRestTemplate template;
	
	
	@Autowired
	private EmployeeUserRepository empRepo;
	
	
	@Autowired
	private ProductVariantsRepository variantRepo;
	
	@Autowired
	private TestHelper helper;
	
	@Test
	void variantCreateNoAuthNTest() {
		HttpEntity<?> request = TestCommons.getHttpEntity("","INVALID TOKEN");
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	@Test
	void productVariantUpdateNoAuthZTest() {
		BaseUserEntity user = empRepo.getById(68L); //not an organization admin
		
		HttpEntity<?> request = TestCommons.getHttpEntity("", user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	void productVariantCreateMissingOprTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductVariantRequest();
		json.remove("operation");
		HttpEntity<?> request = TestCommons.getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}

	@Test
	void addExtraAttributeForProductVariant(){
		BaseUserEntity user = empRepo.getById(69L);

		JSONObject json = getUpdateExtraAttributeRequestBody(1008L, 310008L);

		HttpEntity<?> request = TestCommons.getHttpEntity(json.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = template.exchange("/product/variant"
				, HttpMethod.POST
				, request
				, String.class
		);

		ProductVariantsEntity variantFullData = variantRepo.getVariantFullData(310008L).get();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(3, variantFullData.getExtraAttributes().size());
		assertEquals(2, variantFullData.getFeatureValues().size());
	}

	@Test
	void removeExtraAttributeForProductVariant(){
		BaseUserEntity user = empRepo.getById(70L);

		JSONObject json = getUpdateExtraAttributeRequestBody(1001L, 310001L);

		json.put("extra_attr", "{}");
		json.put("features", "{}");

		HttpEntity<?> request = TestCommons.getHttpEntity(json.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = template.exchange("/product/variant"
				, HttpMethod.POST
				, request
				, String.class
		);

		ProductVariantsEntity variantFullData = variantRepo.getVariantFullData(310001L).get();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(0, variantFullData.getExtraAttributes().size());
		assertEquals(0, variantFullData.getFeatureValues().size());
	}

	private JSONObject getUpdateExtraAttributeRequestBody(Long productId, Long variantId){
		JSONObject json = createProductVariantRequest();
				json.put("variant_id", variantId);
				json.put("product_id", productId);
				json.put("operation", UPDATE.getValue());
		return json;
	}

	@Test
	void productVariantCreateMissingProductTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductVariantRequest();
		json.remove("product_id");
		HttpEntity<?> request = TestCommons.getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	@Test
	void productVariantCreateMissingFeaturesTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductVariantRequest();
		json.remove("features");
		HttpEntity<?> request = TestCommons.getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(OK, response.getStatusCode());
	}
	
	
	
	
	@Test
	void productVariantUpdateMissingVariantTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductVariantRequest();
		json.put("operation", Operation.UPDATE.getValue());
		json.remove("variant_id");
		HttpEntity<?> request = TestCommons.getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	void productVariantCreateTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductVariantRequest();		
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/product/variant"
									, POST
									, request
									, String.class);
		
		assertEquals(OK, response.getStatusCode());
		JSONObject body = new JSONObject(response.getBody());
		Long id = body.getLong("variant_id");
		
		ProductVariantsEntity saved = helper.getVariantFullData(id);
		JSONObject extraAtrrJson = new JSONObject(json.getString("extra_attr"));

		assertVariantDataIsPersisted(json, saved, extraAtrrJson);
		assertInvisibleExtraAttributeIsCreated(saved);
	}

	@Test
	void deleteVariantFeatureValue() {
		HttpEntity<?> request = getHttpEntity("8895ssf");
		ResponseEntity<Void> response = template.exchange("/product/variant_feature?feature_id=237", DELETE, request, Void.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void deleteVariantFeatureValueSingleVariant() {
		HttpEntity<?> request = getHttpEntity("8895ssf");
		ResponseEntity<Void> response = template.exchange("/product/variant_feature?feature_id=237&variant_id=310001",
				DELETE, request, Void.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void deleteExtraAttributeValue() {
		HttpEntity<?> request = getHttpEntity("8895ssf");
		ResponseEntity<Void> response = template.exchange("/product/variant_extra_attribute?extra_attribute_id=337&variant_id=310001",
				DELETE, request, Void.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@ParameterizedTest
	@CsvSource({ "99001,-1,-1", "99001,0,1", "99001,0,1000" })
	void listVariants(Long orgId, Integer start, Integer count) {
		ResponseEntity<VariantsResponse> response = template.getForEntity(
				"/navbox/variants?org_id={orgId}&start={start}&count={count}", VariantsResponse.class, orgId, start,
				count);
		assertEquals(OK, response.getStatusCode());
		assertEquals(4, response.getBody().getTotal());
		if (start > 0 && count > 0)
			assertEquals(Math.min(count, 4 - start), response.getBody().getVariants().size());
	}


	private void assertVariantDataIsPersisted(JSONObject json, ProductVariantsEntity saved, JSONObject extraAtrrJson) {
		assertEquals(json.getString("name"), saved.getName());
		assertEquals(json.getString("barcode"), saved.getBarcode());
		assertEquals(json.getLong("product_id"), saved.getProductEntity().getId().longValue() );
		assertEquals(json.getString("description"), saved.getDescription());
		assertTrue(assertFeatureValuesSaved(json.getString("features"), saved));
		assertEquals(json.getString("sku"), saved.getSku());
		assertEquals(json.getString("product_code"), saved.getProductCode());
		assertEquals("shoe-color-black-shoe-size-37", saved.getPname());
		assertEquals(new BigDecimal(5.5), saved.getWeight());
		assertTrue(extraAtrrJson.similar(getExtraAttributesAsJson(saved)));
	}



	private void assertInvisibleExtraAttributeIsCreated(ProductVariantsEntity saved) {
		boolean isInvisible =
				saved
				.getExtraAttributes()
				.stream()
				.map(ProductExtraAttributesEntity::getExtraAttribute)
				.filter(attr -> attr.getName().startsWith("$"))
				.allMatch(attr -> Objects.equals(INVISIBLE.getValue(), attr.getType()));
		assertTrue(isInvisible);
	}




	private JSONObject getExtraAttributesAsJson(ProductVariantsEntity saved) {
		Map<String,String> extraAttrNameValuePair = 
				saved.getExtraAttributes()
					.stream()
					.collect(toMap(attr -> attr.getExtraAttribute().getName()
								, attr-> attr.getValue()));
		
		return new JSONObject(extraAttrNameValuePair);
	}




	@Test
	void productVariantUpdateTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		ProductVariantsEntity before = variantRepo.findById(TEST_VARIANT_ID).get();
		
		JSONObject json = new JSONObject();
		json.put("operation", UPDATE.getValue());
		json.put("product_id", TEST_PRODUCT_ID);
		json.put("variant_id", TEST_VARIANT_ID);
		json.put("name", "updated");
		json.put("features", "{\"234\": \"30\", \"235\": \"WHITE\"}");
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/product/variant"
									, POST
									, request
									, String.class);
		
		assertEquals(OK, response.getStatusCode());
		JSONObject body = new JSONObject(response.getBody());
		Long id = body.getLong("variant_id");
		
		ProductVariantsEntity saved = helper.getVariantFullData(id);
		
		assertEquals(json.getString("name"), saved.getName());
		assertTrue(assertFeatureValuesSaved(json.getString("features"), saved));
		assertEquals(before.getBarcode(), saved.getBarcode());
		assertEquals(before.getProductEntity().getId(), saved.getProductEntity().getId() );
		assertEquals(before.getDescription(), saved.getDescription());
	}
	

	private boolean assertFeatureValuesSaved(String expectedFeatures, ProductVariantsEntity variant) {
		String features = new JSONObject(variant.getFeatureValues()
				.stream()
				.collect(toMap(f -> f.getFeature().getId(), VariantFeatureValueEntity::getValue)))
				.toString();
		if (expectedFeatures.contains(features))
			return false;
		return true;
	}
	
	
	@Test
	void productVariantUpdateAdminOfOtherOrgTest() {
		BaseUserEntity user = empRepo.getById(70L); //admin from another organization
		
		JSONObject json = createProductVariantRequest();
		json.put("operation", UPDATE.getValue());
		json.put("variant_id", TEST_VARIANT_ID);
		
		HttpEntity<?> request = TestCommons.getHttpEntity(json.toString(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, POST
															, request
															, String.class
															);
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	void productVariantCreateFeaturesFromOtherOrgTest() {
		BaseUserEntity user = empRepo.getById(69L); 
		
		JSONObject json = createProductVariantRequest();
		json.put("features", "{\"236\": 37, \"235\": \"BLack\"}");
		
		HttpEntity<?> request = TestCommons.getHttpEntity(json.toString(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, POST
															, request
															, String.class
															);
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	void productVariantCreateFeaturesNotExistsTest() {
		BaseUserEntity user = empRepo.getById(69L); 
		
		JSONObject json = createProductVariantRequest();
		json.put("features", "{\"888888\": \"37\", \"235\": \"BLack\"}");
		
		HttpEntity<?> request = TestCommons.getHttpEntity(json.toString(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, POST
															, request
															, String.class
															);
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}

	@Test
	void deleteVariants() {
		var request = getHttpEntity("131415");
		var response = template.exchange("/product/variant?variant_id=310002&variant_id=310006", DELETE, request, String.class);
		assertEquals(200, response.getStatusCodeValue());
		List<ProductVariantsEntity> deletedVariants = variantRepo.findByIdIn(asList(310002, 310006));
		deletedVariants.stream().forEach(v -> assertTrue(v.getRemoved() == 1));
	}

	@Test
	void deleteVariantsInDifferentOrg() {
		var request = getHttpEntity("131415");
		var response = template.exchange("/product/variant?variant_id=310001", DELETE, request, String.class);
		assertEquals(406, response.getStatusCodeValue());
	}

	@Test
	void NewProductFlowTest() throws Exception {
		String testImgDir = TEST_IMG_DIR;
		Path img = Paths.get(testImgDir).resolve(TEST_PHOTO).toAbsolutePath();

		byte[] imgData = Files.readAllBytes(img);

		MockMultipartFile imgsPart = new MockMultipartFile("imgs", TEST_PHOTO, "image/png", imgData);

		MockMultipartFile uploadedImagesPriorities = new MockMultipartFile("uploaded_image_priorities",
				"uploaded_image_priorities", MediaType.APPLICATION_JSON_VALUE, "[5]".getBytes());
		
		MockMultipartFile deletedImages = new MockMultipartFile("deleted_images",
				"deleted_images", MediaType.APPLICATION_JSON_VALUE, "[2]".getBytes());

		MockMultipartFile updatedImages = new MockMultipartFile("updated_images",
				"updated_images", MediaType.APPLICATION_JSON_VALUE, "[{\"id\":1,\"priority\":6}]".getBytes());

		JSONObject variant = createProductVariantRequest()
				.put("variant_id", 310002L)
				.put("operation", Operation.UPDATE.getValue());;
		  MockMultipartFile variantJson =
	                new MockMultipartFile(
	                        "var",
	                        "var",
	                        MediaType.APPLICATION_JSON_VALUE,
	                        variant.toString().getBytes(StandardCharsets.UTF_8));

		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.multipart("/product/v2/variant")
				.file(variantJson)
				.file(imgsPart)
				.file(uploadedImagesPriorities)
				.file(deletedImages)
				.file(updatedImages)
				.header(TOKEN_HEADER,"131415"));

		result.andExpect(status().is(200));
	}

	private JSONObject createProductVariantRequest() {
		JSONObject extraAttributes =
				json()
				.put("extra", "Cool Add-on")
				.put("Model", "D2R2")
				.put("$INV", "you can't see me!");
		JSONObject features = json().put("234", "37").put("235", "Black");

		return getJsonVariant(extraAttributes, features);
	}

	private JSONObject getJsonVariant(JSONObject extraAttributes, JSONObject features) {
		JSONObject json = new JSONObject();
		json.put("product_id", TEST_PRODUCT_ID);
		json.put("variant_id", JSONObject.NULL);
		json.put("operation", Operation.CREATE.getValue());
		json.put("name", "AWSOME PRODUCT");
		json.put("p_name", JSONObject.NULL);
		json.put("description", "my description");
		json.put("barcode", "ABC12345");
		json.put("features", features.toString());
		json.put("extra_attr", extraAttributes.toString());
		json.put("sku", "ABC123");
		json.put("product_code", "111-222");
		json.put("weight", 5.5);
		return json;
	}
}
