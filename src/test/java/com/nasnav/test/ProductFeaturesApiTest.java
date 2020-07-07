package com.nasnav.test;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dto.ProductFeatureDTO;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.test.commons.TestCommons;

import net.jcip.annotations.NotThreadSafe;

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
	
	
	@Test
	public void getProductFeaturesTest() throws JsonParseException, JsonMappingException, IOException {
		List<ProductFeatureDTO> expected = 
				Arrays.asList( 
						new ProductFeatureDTO(234, "Shoe size", "Size of the shoes", "s-size", 0),
						new ProductFeatureDTO(235, "Shoe color", "Color of the shoes", "s-color", 0)
					);
		
		Map<String, Object> params = new HashMap<>();
		params.put("organization_id", 99001L);
		
		
		
		String json = template.getForEntity("/organization/products_features?organization_id={organization_id}"
														, String.class
														, params)
												.getBody();
		
		ObjectMapper mapper = new ObjectMapper();
		List<ProductFeatureDTO> fetched = mapper.readValue(json, new TypeReference<List<ProductFeatureDTO>>(){});
		
		assertTrue( expected.stream().allMatch(fetched::contains) );
	}
	
	
	
	
	
	
	
	
	@Test
	public void productFeatureUpdateNoAuthNTest() {
		HttpEntity<?> request = getHttpEntity("","INVALID TOKEN");
		
		ResponseEntity<String> response = template.exchange("/organization/products_feature"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productFeatureUpdateNoAuthZTest() {
		BaseUserEntity user = empRepo.getById(68L);
		
		HttpEntity<?> request = TestCommons.getHttpEntity("", user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/organization/products_feature"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productFeatureCreateMissingParamTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductFeatureRequest();
		json.remove("operation");
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/organization/products_feature"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productFeatureCreatenvalidOprTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductFeatureRequest();
		json.put("operation", "NOT VALID");
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/organization/products_feature"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}


	
	@Test
	public void productFeatureCreateInvalidNameTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductFeatureRequest();
		json.put("name", JSONObject.NULL);
		json.remove("p_name");
		json.remove("description");
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/organization/products_feature"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	@Test
	public void productFeatureCreateOptionalParamAbsentTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductFeatureRequest();
		json.remove("p_name");
		json.remove("description");
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/organization/products_feature"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void productFeatureUpdateNonExistingFeatureIdTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductFeatureRequest();
		json.put("operation", "update");
		json.put("feature_id", 999999999L);
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/organization/products_feature"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productFeatureUpdateMissingFeatureIdTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductFeatureRequest();
		json.put("operation", "update");
		json.remove("feature_id");
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/organization/products_feature"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productFeatureUpdateInvalidUserTest() {
		BaseUserEntity user = empRepo.getById(70L); //Organization admin, but for another organization
		Integer id = TEST_FEATURE_ID;
		
		JSONObject json = createProductFeatureRequest();
		json.put("operation", "update");
		json.put("feature_id", id);
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/organization/products_feature"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productFeatureCreateTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductFeatureRequest();
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/organization/products_feature"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		
		JSONObject body = new JSONObject(response.getBody());
		assertTrue(body.has("feature_id"));
		
		Integer id =  body.getInt("feature_id");		
		Optional<ProductFeaturesEntity> opt= featureRepo.findById(id);
		
		assertTrue(opt.isPresent());
		
		ProductFeaturesEntity saved = opt.get();
		
		assertEquals(json.getString("name"), saved.getName());
		assertEquals(json.getString("description"), saved.getDescription());
		assertEquals(StringUtils.encodeUrl(json.getString("name")) , saved.getPname());		
	}
	
	
	
	
	@Test
	public void productFeatureUpdateTest() {
		BaseUserEntity user = empRepo.getById(69L);
		Integer id = TEST_FEATURE_ID;
		
		ProductFeaturesEntity featureBefore = featureRepo.findById(id).get();
		
		JSONObject json = createProductFeatureRequest();
		json.put("operation", Operation.UPDATE.getValue());
		json.put("feature_id", id);
		json.remove("p_name");
		json.remove("description");
		json.remove("organization");
		
		HttpEntity<?> request = getHttpEntity(json.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/organization/products_feature"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		
		JSONObject body = new JSONObject(response.getBody());
		assertTrue(body.has("feature_id"));
		
		assertEquals(body.get("feature_id"), id);
		
		
		ProductFeaturesEntity saved = featureRepo.findById(id).get();
		
		assertEquals("check updated values",json.getString("name"), saved.getName());
		assertEquals("check values that was not updated", featureBefore.getDescription(), saved.getDescription());
		assertEquals("check values that was not updated", featureBefore.getPname() , saved.getPname());		
	}





	private JSONObject createProductFeatureRequest() {
		JSONObject json = new JSONObject();
		
		json.put("feature_id", JSONObject.NULL);
		json.put("operation", Operation.CREATE.getValue());
		json.put("name", "silly name");
		json.put("p_name", JSONObject.NULL);
		json.put("description", "my description");
		
		return json;
	}
	
	
}
