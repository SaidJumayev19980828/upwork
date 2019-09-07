import static org.junit.Assert.assertEquals;

import java.util.Collection;

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

import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.ProductVariantsEntity;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc 
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Product_Variants_Test_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class ProductVariantApiTest {
	
	private static final Long TEST_VARIANT_ID = 80001L;


	private Long TEST_PRODUCT_ID = 1002L;


	@Autowired
	private TestRestTemplate template;
	
	
	@Autowired
	private EmployeeUserRepository empRepo;
	
	
	@Autowired
	private ProductVariantsRepository variantRepo;
	
	
	
	@Test
	public void variantCreateNoAuthNTest() {
		HttpEntity request = TestCommons.getHttpEntity("",0,"INVALID TOKEN");
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productVariantUpdateNoAuthZTest() {
		BaseUserEntity user = empRepo.getById(68L); //not an organization admin
		
		HttpEntity request = TestCommons.getHttpEntity("", user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productVariantCreateMissingOprTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductVariantRequest();
		json.remove("operation");
		HttpEntity request = TestCommons.getHttpEntity(json.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	@Test
	public void productVariantCreateMissingProductTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductVariantRequest();
		json.remove("product_id");
		HttpEntity request = TestCommons.getHttpEntity(json.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	@Test
	public void productVariantCreateMissingFeaturesTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductVariantRequest();
		json.remove("features");
		HttpEntity request = TestCommons.getHttpEntity(json.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productVariantUpdateMissingVariantTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductVariantRequest();
		json.put("operation", Operation.UPDATE.getValue());
		json.remove("variant_id");
		HttpEntity request = TestCommons.getHttpEntity(json.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void productVariantCreateTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		JSONObject json = createProductVariantRequest();		
		HttpEntity request = TestCommons.getHttpEntity(json.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		JSONObject body = new JSONObject(response.getBody());
		Long id = body.getLong("variant_id");
		
		ProductVariantsEntity saved = variantRepo.findById(id).get();
		
		assertEquals(json.getString("name"), saved.getName());
		assertEquals(json.getString("barcode"), saved.getBarcode());
		assertEquals(json.getLong("product_id"), saved.getProductEntity().getId().longValue() );
		assertEquals(json.getString("description"), saved.getDescription());
		assertEquals(json.getString("features"), saved.getFeatureSpec());
		assertEquals("shoe-size-37-shoe-color-black", saved.getPname());
	}

	
	
	
	@Test
	public void productVariantUpdateTest() {
		BaseUserEntity user = empRepo.getById(69L);
		
		ProductVariantsEntity before = variantRepo.findById(TEST_VARIANT_ID).get();
		
		JSONObject json = new JSONObject();
		json.put("operation", Operation.UPDATE.getValue());
		json.put("product_id", TEST_PRODUCT_ID);
		json.put("variant_id", TEST_VARIANT_ID);
		json.put("name", "updated");
		json.put("features", "{\"234\": 30, \"235\": \"WHITE\"}");
		HttpEntity request = TestCommons.getHttpEntity(json.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		JSONObject body = new JSONObject(response.getBody());
		Long id = body.getLong("variant_id");
		
		ProductVariantsEntity saved = variantRepo.findById(id).get();
		
		assertEquals(json.getString("name"), saved.getName());
		assertEquals(json.get("features"), saved.getFeatureSpec());
		assertEquals(before.getBarcode(), saved.getBarcode());
		assertEquals(before.getProductEntity().getId(), saved.getProductEntity().getId() );
		assertEquals(before.getDescription(), saved.getDescription());
		
	}
	


	
	
	@Test
	public void productVariantUpdateAdminOfOtherOrgTest() {
		BaseUserEntity user = empRepo.getById(70L); //admin from another organization
		
		JSONObject json = createProductVariantRequest();
		json.put("operation", Operation.UPDATE.getValue());
		json.put("variant_id", TEST_VARIANT_ID);
		
		HttpEntity request = TestCommons.getHttpEntity(json.toString(), user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void productVariantCreateFeaturesFromOtherOrgTest() {
		BaseUserEntity user = empRepo.getById(69L); 
		
		JSONObject json = createProductVariantRequest();
		json.put("features", "{\"236\": 37, \"235\": \"BLack\"}");
		
		HttpEntity request = TestCommons.getHttpEntity(json.toString(), user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void productVariantCreateFeaturesNotExistsTest() {
		BaseUserEntity user = empRepo.getById(69L); 
		
		JSONObject json = createProductVariantRequest();
		json.put("features", "{\"888888\": 37, \"235\": \"BLack\"}");
		
		HttpEntity request = TestCommons.getHttpEntity(json.toString(), user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = template.exchange("/product/variant"
															, HttpMethod.POST
															, request
															, String.class
															);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	

	private JSONObject createProductVariantRequest() {
		JSONObject json = new JSONObject();
		
		json.put("product_id", TEST_PRODUCT_ID);
		json.put("variant_id", JSONObject.NULL);
		json.put("operation", Operation.CREATE.getValue());
		json.put("name", "AWSOME PRODUCT");
		json.put("p_name", JSONObject.NULL);
		json.put("description", "my description");
		json.put("barcode", "ABC12345");
		json.put("features", "{\"234\": 37, \"235\": \"BLack\"}");
		
		return json;
	}
	
	
	
	
}
