package com.nasnav.test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ExtraAttributesRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dto.ProductFeatureDTO;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.test.commons.TestCommons;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.StringUtils.encodeUrl;
import static com.nasnav.constatnts.EntityConstants.Operation.UPDATE;
import static com.nasnav.enumerations.ProductFeatureType.*;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc 
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Product_Features_Test_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class ProductFeaturesApiTest {
	
	private static final int TEST_FEATURE_ID = 236;


	@Autowired
	private TestRestTemplate template;
	
	
	@Autowired
	private EmployeeUserRepository empRepo;
	
	
	@Autowired
	private ProductFeaturesRepository featureRepo;


	@Autowired
	private ExtraAttributesRepository extraAttrRepo;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void getProductFeaturesTest() throws JsonParseException, JsonMappingException, IOException {
		var expected =
			asList(
				productFeatureDTO(234, "Shoe size", "Size of the shoes", "s-size", 0, STRING),
				productFeatureDTO(235, "Shoe color", "Color of the shoes", "s-color", 0, STRING)
				);
		
		Map<String, Object> params = new HashMap<>();
		params.put("organization_id", 99001L);

		var json = template.getForEntity("/organization/products_features?organization_id={organization_id}"
														, String.class
														, params)
												.getBody();
		var mapper = new ObjectMapper();
		var fetched = mapper.readValue(json, new TypeReference<List<ProductFeatureDTO>>(){});
		
		assertTrue(fetched.containsAll(expected));
	}



	private ProductFeatureDTO productFeatureDTO(int id, String name, String description, String pname, int level, ProductFeatureType type) {
		var dto = new ProductFeatureDTO();
		dto.setId(id);
		dto.setName(name);
		dto.setDescription(description);
		dto.setPname(pname);
		dto.setPname(pname);
		dto.setLevel(level);
		dto.setType(type);
		dto.setExtraData(emptyMap());
		return dto;
	}



	@Test
	public void getProductFeaturesTypes() throws IOException {
		BaseUserEntity user = empRepo.getById(68L);
		HttpEntity<?> request = getHttpEntity(user.getAuthenticationToken());
		var response =
				template.exchange("/organization/products_features/types"
						, GET
						, request
						, String.class
						);

		assertEquals(OK, response.getStatusCode());

		var expectedTypes  = setOf(ProductFeatureType.values()).stream().map(ProductFeatureType::name).collect(toSet());
		var types = objectMapper.readValue(response.getBody(), new TypeReference<Set<String>>(){});
		assertEquals(expectedTypes, types);
	}
	
	
	
	
	
	@Test
	public void productFeatureUpdateNoAuthNTest() {
		HttpEntity<?> request = getHttpEntity("","INVALID TOKEN");

		var response = template.exchange("/organization/products_feature"
															, POST
															, request
															, String.class
															);
		
		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productFeatureUpdateNoAuthZTest() {
		BaseUserEntity user = empRepo.getById(68L);
		
		HttpEntity<?> request = TestCommons.getHttpEntity("", user.getAuthenticationToken());

		var response = template.exchange("/organization/products_feature"
															, POST
															, request
															, String.class
															);
		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productFeatureCreateMissingParamTest() {
		BaseUserEntity user = empRepo.getById(69L);

		var json = createProductFeatureRequest();
		json.remove("operation");
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());

		var response = template.exchange("/organization/products_feature"
															, POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productFeatureCreateNonValidOprTest() {
		BaseUserEntity user = empRepo.getById(69L);

		var json = createProductFeatureRequest();
		json.put("operation", "NOT VALID");
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());

		var response = template.exchange("/organization/products_feature"
															, POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}


	
	@Test
	public void productFeatureCreateInvalidNameTest() {
		BaseUserEntity user = empRepo.getById(69L);

		var json = createProductFeatureRequest();
		json.put("name", JSONObject.NULL);
		json.remove("p_name");
		json.remove("description");
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());

		var response = template.exchange("/organization/products_feature"
															, POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	@Test
	public void productFeatureCreateOptionalParamAbsentTest() {
		BaseUserEntity user = empRepo.getById(69L);

		var json = createProductFeatureRequest();
		json.remove("p_name");
		json.remove("description");
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());

		var response = template.exchange("/organization/products_feature"
															, POST
															, request
															, String.class
															);
		
		assertEquals(OK, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void productFeatureUpdateNonExistingFeatureIdTest() {
		BaseUserEntity user = empRepo.getById(69L);

		var json = createProductFeatureRequest();
		json.put("operation", "update");
		json.put("feature_id", 999999999L);
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());

		var response = template.exchange("/organization/products_feature"
															, POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productFeatureUpdateMissingFeatureIdTest() {
		BaseUserEntity user = empRepo.getById(69L);

		var json = createProductFeatureRequest();
		json.put("operation", "update");
		json.remove("feature_id");
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());

		var response = template.exchange("/organization/products_feature"
															, POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productFeatureUpdateInvalidUserTest() {
		BaseUserEntity user = empRepo.getById(70L); //Organization admin, but for another organization
		Integer id = TEST_FEATURE_ID;

		var json = createProductFeatureRequest();
		json.put("operation", "update");
		json.put("feature_id", id);
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());

		var response = template.exchange("/organization/products_feature"
															, POST
															, request
															, String.class
															);
		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}



	@Test
	public void productFeatureCreateWithDuplicateNameTest() {
		BaseUserEntity user = empRepo.getById(69L);

		var json = createProductFeatureRequest();
		json.put("name", "Shoe size");
		HttpEntity<?> request = getHttpEntity(json.toString(), user.getAuthenticationToken());

		var response =
				template.exchange("/organization/products_feature"
						, POST
						, request
						, String.class
				);
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}



	@Test
	public void productFeatureCreateWithDuplicateNameInOtherOrgTest() {
		BaseUserEntity user = empRepo.getById(70L);

		var json = createProductFeatureRequest();
		json.put("name", "Shoe color");
		HttpEntity<?> request = getHttpEntity(json.toString(), user.getAuthenticationToken());

		var response =
				template.exchange("/organization/products_feature"
						, POST
						, request
						, String.class
				);
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}


	
	@Test
	public void productFeatureCreateTest() {
		var json = createProductFeatureRequest();
		var opt= postProductFeature(json);
		
		assertTrue(opt.isPresent());

		var saved = opt.get();
		
		assertEquals(json.getString("name"), saved.getName());
		assertEquals(json.getString("description"), saved.getDescription());
		assertEquals(encodeUrl(json.getString("name")) , saved.getPname());
		assertEquals(STRING.getValue() , saved.getType());
	}



	@Test
	public void productFeatureCreateWithTypeTest() {
		var json = createProductFeatureRequest();
		json.put("type", COLOR.name());

		var opt = postProductFeature(json);

		assertTrue(opt.isPresent());

		var savedPName = encodeUrl(json.getString("name"));
		var saved = opt.get();
		assertEquals(json.getString("name"), saved.getName());
		assertEquals(json.getString("description"), saved.getDescription());
		assertEquals(savedPName, saved.getPname());
		assertEquals(json.getString("type") , getTypeName(saved));
		assertSwatchExtraAttrCreated(savedPName, saved);
	}



	private Optional<ProductFeaturesEntity> postProductFeature(JSONObject json) {
		var user = empRepo.getById(69L);
		var request = getHttpEntity(json.toString() , user.getAuthenticationToken());

		var response =
				template.exchange("/organization/products_feature"
						, POST
						, request
						, String.class
				);
		assertEquals(OK, response.getStatusCode());

		var body = new JSONObject(response.getBody());
		assertTrue(body.has("feature_id"));

		var id =  body.getInt("feature_id");
		return featureRepo.findById(id);
	}



	private void assertSwatchExtraAttrCreated(String savedPName, ProductFeaturesEntity saved) {
		var attr =
				extraAttrRepo
						.findByNameAndOrganizationId(format("$%s$%s", savedPName, COLOR.name()),99002L);
		assertTrue(attr.isPresent());
		assertEquals(attr.get().getId().intValue(), getExtraDataExtraAttr(saved));
	}



	private int getExtraDataExtraAttr(ProductFeaturesEntity saved) {
		return new JSONObject(saved.getExtraData()).getInt("extra_attribute_id");
	}



	private String getTypeName(ProductFeaturesEntity saved) {
		return getProductFeatureType(saved.getType()).get().name();
	}



	@Test
	public void productFeatureUpdateTest() {
		BaseUserEntity user = empRepo.getById(69L);
		Integer id = TEST_FEATURE_ID;

		var featureBefore = featureRepo.findById(id).get();

		var json = createProductFeatureRequest();
		json.put("operation", UPDATE.getValue());
		json.put("feature_id", id);
		json.remove("p_name");
		json.remove("description");
		json.remove("organization");
		
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());

		var response = template.exchange("/organization/products_feature"
															, POST
															, request
															, String.class
															);
		assertEquals(OK, response.getStatusCode());

		var body = new JSONObject(response.getBody());
		assertTrue(body.has("feature_id"));
		
		assertEquals(body.get("feature_id"), id);

		var saved = featureRepo.findById(id).get();
		
		assertEquals("check updated values",json.getString("name"), saved.getName());
		assertEquals("check values that was not updated", featureBefore.getDescription(), saved.getDescription());
		assertEquals("check values that was not updated", featureBefore.getPname() , saved.getPname());		
	}



	@Test
	public void productFeatureUpdateToColorTypeTest() {
		var json = createProductFeatureRequest();
		json.put("operation", UPDATE.getValue());
		json.put("feature_id", TEST_FEATURE_ID);
		json.put("type", COLOR.name());

		var saved = postProductFeature(json).get();
		var updatedType = ProductFeatureType.getProductFeatureType(saved.getType()).get().name();
		assertEquals("check updated values",json.getString("type"), updatedType);
		assertSwatchExtraAttrCreated(saved.getPname(), saved);
	}



	@Test
	public void productFeatureUpdateToTextThenBackToColorTypeTest() {
		Integer id = createColorFeature();
		updateToStringFeature(id);
		var secondUpdateJson =
				createProductFeatureRequest()
						.put("operation", UPDATE.getValue())
						.put("feature_id", id)
						.put("type", COLOR.name());
		var saved = postProductFeature(secondUpdateJson).get();
		var updatedType = ProductFeatureType.getProductFeatureType(saved.getType()).get().name();
		assertEquals("check updated values",secondUpdateJson.getString("type"), updatedType);
		assertSwatchExtraAttrCreated(saved.getPname(), saved);
	}



	private void updateToStringFeature(Integer id) {
		var firstUpdateJson =
				createProductFeatureRequest()
						.put("operation", UPDATE.getValue())
						.put("feature_id", id)
						.put("type", STRING.name());
		postProductFeature(firstUpdateJson).get();
	}



	private Integer createColorFeature() {
		var createJson = createProductFeatureRequest().put("type", COLOR.name());
		return postProductFeature(createJson).get().getId();
	}



	@Test
	public void deleteFeatureNoAuthN(){
		HttpEntity<?> request = getHttpEntity("INVALID");

		var response =
				template.exchange("/organization/products_feature?id=234", DELETE, request, String.class);

		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}



	@Test
	public void deleteFeatureNoAuthZ(){
		HttpEntity<?> request = getHttpEntity("192021");

		var response =
				template.exchange("/organization/products_feature?id=234", DELETE, request, String.class);

		assertEquals(FORBIDDEN, response.getStatusCode());
	}



	@Test
	public void deleteFeatureFromAnotherOrg(){
		HttpEntity<?> request = getHttpEntity("131415");

		var response =
				template.exchange("/organization/products_feature?id=234", DELETE, request, String.class);

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}



	@Test
	public void deleteFeatureStillUsed(){
		HttpEntity<?> request = getHttpEntity("161718");

		var response =
				template.exchange("/organization/products_feature?id=235", DELETE, request, String.class);

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}



	@Test
	public void deleteFeatureAlreadyDeleted(){
		HttpEntity<?> request = getHttpEntity("131415");

		var response =
				template.exchange("/organization/products_feature?id=2333337", DELETE, request, String.class);

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}



	@Test
	public void deleteFeatureSuccess(){
		var featureId = 234;

		Optional<?> featureBefore = featureRepo.findById(featureId);
		assertTrue(featureBefore.isPresent());

		HttpEntity<?> request = getHttpEntity("161718");

		var response =
				template.exchange("/organization/products_feature?id=" + featureId, DELETE, request, String.class);

		assertEquals(OK, response.getStatusCode());

		Optional<?> featureAfter = featureRepo.findById(featureId);
		assertFalse(featureAfter.isPresent());
	}



	@Test
	public void updateFeatureDeleted(){
		BaseUserEntity user = empRepo.getById(69L);
		Integer id = 237;

		var featureExists = featureRepo.existsById(id);
		assertFalse(featureExists);

		var json = createProductFeatureRequest();
		json.put("operation", UPDATE.getValue());
		json.put("feature_id", id);
		json.remove("p_name");

		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());

		var response = template.exchange("/organization/products_feature"
				, POST
				, request
				, String.class
		);
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}




	private JSONObject createProductFeatureRequest() {
		var json = new JSONObject();
		
		json.put("feature_id", JSONObject.NULL);
		json.put("operation", Operation.CREATE.getValue());
		json.put("name", "silly name");
		json.put("p_name", JSONObject.NULL);
		json.put("description", "my description");

		
		return json;
	}
	
	
}
