import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;

import javax.persistence.criteria.Path;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.service.ProductService;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_API_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ProductApiTest {
	
	@Autowired
	private TestRestTemplate template;

	
	@Autowired 
	private EmployeeUserRepository empUserRepo;
	
	@Autowired
	private ProductRepository productRepository;
	
	
	@Autowired
	private ProductImagesRepository imgRepo;;
	
	@Test
	public void createProductUserWithNoRightsTest() throws JsonProcessingException {
		BaseUserEntity user = empUserRepo.getById(68L);
		
		JSONObject productJson = createNewDummyProduct();		
		
		HttpEntity request =  TestCommons.getHttpEntity(productJson.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/product/info"
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		
		JSONObject body = new JSONObject(response.getBody());
		assertFalse(body.getBoolean("success"));
	}
	
	
	
	
	@Test
	public void createProductTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		
		ResponseEntity<ProductUpdateResponse> response = postProductData(user, product);		
		
		validateCreatedProductResponse(response);
		
		Long id = response.getBody().getProductId();
		ProductEntity saved  = productRepository.findById(id).get();	
		
		validateCreatedProductData(product, saved, id, user.getOrganizationId());
	}
	
	

	private void validateCreatedProductResponse(ResponseEntity<ProductUpdateResponse> response) {
		assertEquals(HttpStatus.OK, response.getStatusCode());	
		ProductUpdateResponse body = response.getBody();
		assertTrue( body.isSuccess());
		assertNotEquals(body.getProductId() , Long.valueOf(0L));
		assertTrue(productRepository.existsById(body.getProductId()));
	}
	
	
	
	
	@Test
	public void updateProductTest() throws JsonProcessingException{		
		BaseUserEntity user = empUserRepo.getById(69L);
		Long id = 1001L;
		ProductEntity originalProduct =  productRepository.findById(id).get();		
		
		
		//modify some fields for existing product in the test data, only the fields added to the JSON will be changed
		JSONObject product = new JSONObject();
		product.put("operation", Operation.UPDATE.getValue());
		product.put("product_id",id);  
		product.put("barcode","UPDATEDbARcODE");
		product.put("name","updated product");
		product.put("brand_id", JSONObject.NULL);
		
		HttpEntity request =  TestCommons.getHttpEntity(product.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<ProductUpdateResponse> response = 
				template.exchange("/product/info"
						, HttpMethod.POST
						, request
						, ProductUpdateResponse.class);	
		
		validateCreatedProductResponse(response);
		
		Long savedId = response.getBody().getProductId();
		ProductEntity saved  = productRepository.findById(savedId).get();	
		
		//modified properties should equal to new values
		assertEquals(id , saved.getId());
		assertEquals(product.getString("name"), saved.getName());
		assertEquals(product.getString("barcode"), saved.getBarcode());
		assertNull(saved.getBrandId());
		
		//original values should remain the same
		assertEquals("updated-product", saved.getPname());
		assertEquals(originalProduct.getDescription(), saved.getDescription());		
		assertEquals(originalProduct.getCategoryId(), saved.getCategoryId());
		assertEquals(user.getOrganizationId() , saved.getOrganizationId()); 
	}





	private ResponseEntity<ProductUpdateResponse> postProductData(BaseUserEntity user, JSONObject productJson)
			throws JsonProcessingException {
		
		HttpEntity request =  TestCommons.getHttpEntity(productJson.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<ProductUpdateResponse> response = 
				template.exchange("/product/info"
						, HttpMethod.POST
						, request
						, ProductUpdateResponse.class);
		return response;
	}
	
	
	
	
	private void validateCreatedProductData(JSONObject product, ProductEntity saved, Long id, Long userOrgId) {
		assertEquals(id , saved.getId());
		assertEquals(product.get("name"), saved.getName());
		assertEquals(product.get("p_name"), saved.getPname());
		assertEquals(product.get("description"), saved.getDescription());
		assertEquals(product.get("barcode"), saved.getBarcode());
		assertEquals(product.get("brand_id"), saved.getBrandId());
		assertEquals(product.get("category_id"), saved.getCategoryId());
		assertEquals(userOrgId, saved.getOrganizationId()); //the new product takes the organization of the user 
	}
	
	

	private JSONObject createNewDummyProduct() {
		JSONObject product = new JSONObject();
		product.put("operation", Operation.CREATE.getValue());
		product.put("product_id", JSONObject.NULL);
		product.put("name", "Test Product");
		product.put("p_name", "test_product");
		product.put("description", "Testing creating/updating product");
		product.put("barcode", "BAR12345CODE");
		product.put("brand_id", 101L);
		product.put("category_id", 201L);
		
		return product;
	}
	
	
	
	
	@Test
	public void createProdcutDefaultPnameTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.put("p_name",JSONObject.NULL);  //set pname to null, so that it will be auto-generated from product name
		
		ResponseEntity<ProductUpdateResponse> response = postProductData(user, product);		
		
		validateCreatedProductResponse(response);
		
		Long id = response.getBody().getProductId();
		ProductEntity saved  = productRepository.findById(id).get();
		
		product.put("p_name","test-product"); //set the expected pname , it will be compared against the generated one in the entity
		
		validateCreatedProductData(product, saved, id, user.getOrganizationId());
	}
	
	
	
	
	@Test
	public void createProdcutNullBrand() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.put("brand_id", JSONObject.NULL);  
		
		ResponseEntity<String> response = postInvalidProductData(user, product);	
		
		Long id = new JSONObject(response.getBody()).getLong("product_id");
		ProductEntity saved  = productRepository.findById(id).get();
		
		validateCreatedProductData(product, saved, id, user.getOrganizationId());
	}
	
	
	
	
	@Test
	public void createProdcutNonExistingBrand() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.put("brand_id", 99999L);  
		
		ResponseEntity<String> response = postInvalidProductData(user, product);		
		
		validateErrorResponse(response);
	}
	
	
	
	@Test
	public void createProdcutInvalidBrand() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.put("brand_id", 102L); // this brand is for another organization 
		
		ResponseEntity<String> response = postInvalidProductData(user, product);		
		
		validateErrorResponse(response);
	}

	
	@Test
	public void createProdcutNonExistingCategory() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.put("category_id", 99999L);  
		
		ResponseEntity<String> response = postInvalidProductData(user, product);		
		
		validateErrorResponse(response);
	}


	
	@Test
	public void createProdcutNullCategory() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.put("category_id",JSONObject.NULL);  
		
		ResponseEntity<String> response = postInvalidProductData(user, product);		
		
		validateErrorResponse(response);
	}
	
	
	@Test
	public void createProdcutNullName() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.put("name", JSONObject.NULL);  
		
		ResponseEntity<String> response = postInvalidProductData(user, product);		
		
		validateErrorResponse(response);
	}
	
	
	
	
	@Test
	public void updateProdcutNonExistingOperationField() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.remove("operation"); 
		
		ResponseEntity<String> response = postInvalidProductData(user, product);		
		
		validateErrorResponse(response);
	}
	
	
	
	@Test
	public void createProdcutNonExistingNameField() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.remove("name"); 
		
		ResponseEntity<String> response = postInvalidProductData(user, product);		
		
		validateErrorResponse(response);
	}
	
	
	
	@Test
	public void createProdcutNonExistingCategoryField() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.remove("category_id"); 
		
		ResponseEntity<String> response = postInvalidProductData(user, product);		
		
		validateErrorResponse(response);
	}
	
	
	
	@Test
	public void createProdcutNonExistingBrandIdField() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.remove("brand_id"); 
		
		ResponseEntity<String> response = postInvalidProductData(user, product);		
		
		validateErrorResponse(response);
	}
	
	
	
	
	@Test
	public void updateProdcutNonExistingIdField() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		
		JSONObject product = createNewDummyProduct();
		product.put("operation", Operation.UPDATE.getValue());
		product.remove("product_id"); 
		
		ResponseEntity<String> response = postInvalidProductData(user, product);		
		
		validateErrorResponse(response);
	}
	


	private void validateErrorResponse(ResponseEntity<String> response) {
		JSONObject body = new JSONObject(response.getBody()); 
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());		
		assertTrue(body.has("message"));
		assertTrue(body.has("error"));
	}
	


	private ResponseEntity<String> postInvalidProductData(BaseUserEntity user, JSONObject productJson)
			throws JsonProcessingException {				
		
		HttpEntity request =  TestCommons.getHttpEntity(productJson.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/product/info"
						, HttpMethod.POST
						, request
						, String.class);
		return response;
	} 
	
	
	
	
	
	
	@Test
	public void deleteProductTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);
		
		Long productId = 1008L; 
		
		
		assertTrue(productRepository.existsById(productId)); //assert product exists before delete
		assertNotEquals("product had images", 0, imgRepo.findByProductEntity_Id(productId).size());
		
		ResponseEntity<String> response = deleteProduct(user, productId);		
		
		ObjectMapper mapper = new ObjectMapper();
		ProductUpdateResponse body = mapper.readValue(response.getBody(), ProductUpdateResponse.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(body.isSuccess());
		assertEquals(productId, body.getProductId());
		assertFalse(productRepository.existsById(productId));
		assertEquals("product images was deleted", 0, imgRepo.findByProductEntity_Id(productId).size());
	}
	
	
	
	
	@Test
	public void deleteProductInvalidUserRoleTest() {
		BaseUserEntity user = empUserRepo.getById(68L); //this user has NASNAV_ADMIN Role
		
		Long productId = 1006L; 
		
		assertTrue(productRepository.existsById(productId)); //assert product exists before delete
		
		ResponseEntity<String> response = deleteProduct(user, productId);		
		
		JSONObject body = new JSONObject(response.getBody());
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	@Test
	public void deleteProductInvalidUserOrgTest() {
		EmployeeUserEntity user = empUserRepo.getById(69L); //set another organization for the user
		user.setOrganizationId(99001L);
		empUserRepo.save(user);
		
		Long productId = 1006L; 
		
		assertTrue(productRepository.existsById(productId)); //assert product exists before delete
		
		ResponseEntity<String> response = deleteProduct(user, productId);
		
		JSONObject body = new JSONObject(response.getBody());
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	@Test
	public void deleteProductNonExistingUserTest() {
		
		Long productId = 1006L; 
		
		assertTrue(productRepository.existsById(productId)); //assert product exists before delete
		
		HttpEntity request =  TestCommons.getHttpEntity("" , 4444, "InvalidToken");
		
		ResponseEntity<String> response = 
				template.exchange("/product?product_id=" + productId
						, HttpMethod.DELETE
						, request
						, String.class);
		
		JSONObject body = new JSONObject(response.getBody());
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	@Test
	public void deleteProductHasStocksTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);
		
		Long productId = 1006L; 
		
		assertTrue("assert product exists before delete", productRepository.existsById(productId)); //
		
		ResponseEntity<String> response = deleteProduct(user, productId);		
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertTrue("assert product still exists after delete", productRepository.existsById(productId));		
	}
	
	
	
	@Test
	public void deleteProductInBundleTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);
		
		Long productId = 1003L; 
		
		assertTrue("assert product exists before delete", productRepository.existsById(productId)); //
		assertNotEquals("product had images", 0, imgRepo.findByProductEntity_Id(productId).size());
		
		ResponseEntity<String> response = deleteProduct(user, productId);		
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertTrue("assert product still exists after delete", productRepository.existsById(productId));
		assertNotEquals("product images was NOT deleted", 0, imgRepo.findByProductEntity_Id(productId).size());
	}




	private ResponseEntity<String> deleteProduct(BaseUserEntity user, Long productId) {
		HttpEntity request =  TestCommons.getHttpEntity("" , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/product?product_id=" + productId
						, HttpMethod.DELETE
						, request
						, String.class);
		return response;
	}

}
