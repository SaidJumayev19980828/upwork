package com.nasnav.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.commons.enums.SortOrder;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.BundleRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.BundleDTO;
import com.nasnav.dto.ProductSortOptions;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.BundleEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductTypes;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.response.BundleResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.ProductsDeleteResponse;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.helpers.TestHelper;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Bundle_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class BundlesApiTest {
	
	private static final long VALID_ORG_AMDIN = 69L;


	private static final long OTHER_ORG_ADMIN = 70L;


	private static final long NOT_ORG_ADMIN = 68L;


	@Autowired
	private TestRestTemplate template;
	
	
	@Autowired
	private BundleRepository bundleRepo;
	
	
	@Autowired
	private EmployeeUserRepository empUserRepo;
	
	
	@Autowired
	private ProductRepository productRepo;
	
	
	@Autowired
	private JdbcTemplate jdbc;
	
	
	@Autowired
	private BasketRepository basketRepo;
	
	
	@Value("classpath:/json/bundle_api_test/bundle_api_test_single_bundle_respose.json")
	private Resource singleBundleResponse;
	
	
	@Autowired
	private TestHelper helper;
	
	@Test	
	public void getBundlesTest() throws JsonParseException, JsonMappingException, IOException{
		Long orgId = 99001L;
		int expectedCount = 2;
		
		String url = "/product/bundles?"
							+ "org_id=" + orgId
							+ "&count=" + expectedCount
							+ "&sort=" + ProductSortOptions.NAME.getValue()
							+ "&order=" + SortOrder.DESC.getValue();
		ResponseEntity<String> response = performHttpGet(url); 
		
		
		assertEquals( HttpStatus.OK,response.getStatusCode());
		
		String body = response.getBody();
		System.out.println("response >>>" + body);
		
		ObjectMapper mapper = new ObjectMapper();
		BundleResponse bundleRes = mapper.readValue(body, BundleResponse.class);
		
		assertEquals(expectedCount, bundleRes.getBundles().size());
		
		
		
		Long expectedTotal = bundleRepo.countByOrganizationId(orgId);
		assertEquals("expected number of all bundles for the organization, before limiting the result with 'count' parameter"
						, expectedTotal
						, bundleRes.getTotal() );
		
		
		BundleDTO firstBundle = bundleRes.getBundles().get(0);
		BundleEntity expectedFirstBundle = bundleRepo.findFirstByOrderByNameDesc();
		assertEquals("we order bundles desc by name, so , we expect this to be the first"
							, expectedFirstBundle.getName() 
							, firstBundle.getName());
	}
	
	
	
	
	
	@Test
	public void getSingleBundleTest() throws JsonParseException, JsonMappingException, IOException{
		Long bundleId = 200005L;
		
		
		String url = "/product/bundles?"
								+ "bundle_id=" + bundleId
								+ "&sort=" + ProductSortOptions.NAME.getValue()
								+ "&order=" + SortOrder.DESC.getValue();
		ResponseEntity<String> response = performHttpGet(url); 

		assertEquals( HttpStatus.OK,response.getStatusCode());
		
		String body = response.getBody();
		System.out.println("response >>>" + body);
		
		ObjectMapper mapper = new ObjectMapper();
		BundleResponse bundleRes = mapper.readValue(body, BundleResponse.class);
		
		BundleResponse expectedResponse = mapper.readValue(singleBundleResponse.getInputStream(), BundleResponse.class);
		
		assertEquals(1, bundleRes.getBundles().size());
		assertEquals(expectedResponse, bundleRes);		
		assertEquals("expected number of all bundles that applies to the query"
						, Long.valueOf(1)
						, bundleRes.getTotal() );
		
		
		BundleDTO firstBundle = bundleRes.getBundles().get(0);
		BundleEntity expectedBundle = bundleRepo.findById(bundleId).get();
		assertEquals("we order bundles desc by name, so , we expect this to be the first"
							, expectedBundle.getName() 
							, firstBundle.getName());
	}
	
	
	
	
	@Test	
	public void getBundleMissingParamsTest() throws JsonParseException, JsonMappingException, IOException{
		
		String url = "/product/bundles";		
		ResponseEntity<String> response = performHttpGet(url); 
			
		
		assertEquals( HttpStatus.NOT_ACCEPTABLE,response.getStatusCode());
		System.out.println("response >>>" + response);
	}





	private ResponseEntity<String> performHttpGet(String url) {				
		ResponseEntity<String> response = template.getForEntity(url, String.class);
		return response;
	}
	
	
	
	
	/*@Test
	public void getBundlesByCategoryTest() throws JsonParseException, JsonMappingException, IOException{
		Long orgId = 99001L;
		Long categoryId = 201L;
		int expectedCount = 2;	
		
		String url = "/product/bundles?"
							+ "org_id=" + orgId
							+ "&category_id=" + categoryId
							+ "&count=" + expectedCount
							+ "&sort=" + ProductSortOptions.NAME.getValue();
		ResponseEntity<String> response = performHttpGet(url); 
		
		
		assertEquals( HttpStatus.OK,response.getStatusCode());
		
		String body = response.getBody();
		System.out.println("response >>>" + body);
		
		ObjectMapper mapper = new ObjectMapper();
		BundleResponse bundleRes = mapper.readValue(body, BundleResponse.class);
		
		assertEquals(expectedCount, bundleRes.getBundles().size());
		
		
		
		Long expectedTotal = bundleRepo.countByCategoryId(categoryId);
		assertEquals("expected number of all bundles for the category, before limiting the result with 'count' parameter"
						, expectedTotal
						, bundleRes.getTotal() );
		
		
		BundleDTO firstBundle = bundleRes.getBundles().get(0);
		BundleEntity expectedFirstBundle = bundleRepo.findFirstByCategoryIdOrderByNameAsc(categoryId);
		assertEquals("we order bundles ASC by name, so , we expect this to be the first"
							, expectedFirstBundle.getName() 
							, firstBundle.getName());
	}*/
	
	
	
	
	
	@Test
	public void createBundleNoAuthNTest() throws JsonProcessingException{
		
		JSONObject bundle = createNewDummyProduct();
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(bundle.toString(), "non-existing-token");
		
		ResponseEntity<String> response = 
				template.exchange("/product/bundles"
						, HttpMethod.POST
						, request
						, String.class);		
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void createBundleUnAuthZTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(68L); //this user is not an organization admin
		
		JSONObject bundle = createNewDummyProduct();
		
		ResponseEntity<ProductUpdateResponse> response = postProductData(user, bundle);		
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void createBundleTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L); 
		
		JSONObject bundle = createNewDummyProduct();
		
		ResponseEntity<ProductUpdateResponse> response = postProductData(user, bundle);		
		
		validateCreatedProductResponse(response);
		
		Long id = response.getBody().getProductId();
		BundleEntity saved  = bundleRepo.findById(id).get();	
		
		validateCreatedBundleData(bundle, saved, id, user.getOrganizationId());
	}
	
	

	private void validateCreatedProductResponse(ResponseEntity<ProductUpdateResponse> response) {
		assertEquals(OK, response.getStatusCode());	
		ProductUpdateResponse body = response.getBody();
		assertNotEquals(body.getProductId() , Long.valueOf(0L));
		assertTrue(bundleRepo.existsById(body.getProductId()));
	}
	
	
	
	private ResponseEntity<ProductUpdateResponse> postProductData(BaseUserEntity user, JSONObject productJson)
			throws JsonProcessingException {
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(productJson.toString() , user.getAuthenticationToken());
		
		ResponseEntity<ProductUpdateResponse> response = 
				template.exchange("/product/bundle"
						, POST
						, request
						, ProductUpdateResponse.class);
		return response;
	}
	
	
	
	
	private void validateCreatedBundleData(JSONObject product, ProductEntity saved, Long id, Long userOrgId) {
		assertEquals(id , saved.getId());
		assertEquals(product.get("name"), saved.getName());
		assertEquals(product.get("p_name"), saved.getPname());
		assertEquals(product.get("description"), saved.getDescription());
		assertEquals(product.get("barcode"), saved.getBarcode());
		assertEquals(product.get("brand_id"), saved.getBrandId());
		assertEquals(userOrgId, saved.getOrganizationId()); //the new product takes the organization of the user
		assertEquals(ProductTypes.BUNDLE, saved.getProductType().intValue());
	}
	
	

	private JSONObject createNewDummyProduct() {
		JSONObject product = new JSONObject();
		product.put("operation", Operation.CREATE.getValue());
		product.put("product_id", JSONObject.NULL);
		product.put("name", "Test Bundle");
		product.put("p_name", "test_bundle");
		product.put("description", "Testing creating/updating bundle");
		product.put("barcode", "BAR12345CODE");
		product.put("brand_id", 101L);
		product.put("category_id", 201L);		
		return product;
	}
	
	
	
	
	
	
	@Test		
	public void bundleDeleteTest() {
		Long bundleId = 200004L;
		Long stockId =  getBundleVirtualStock(bundleId);
		
		assertNotNull("assert bundle already exists", bundleRepo.existsById(bundleId));
		assertNotNull("assert bundle has a virtual stock", stockId);

		BaseUserEntity user = empUserRepo.getById(69L); 
		ResponseEntity<ProductsDeleteResponse> response = deleteBundle(bundleId, user);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(bundleId, response.getBody().getProductIds().get(0));
		assertFalse("assert bundle doesn't exists", bundleRepo.existsById(bundleId));
	}
	
	
	//couldn't use @Transactional on the test method, because it miss up with committing the scripts that @Sql runs
	//can't use @Transactional on methods used internally because spring uses proxies to manage the transactions.
	//and proxies work when the method is called from extrnal class only.
	//That's why i used a native query.
	public Long getBundleVirtualStock(Long bundleId) {
		return jdbc.queryForObject("select id from stocks where variant_id= (select id from product_variants where product_id="+bundleId+")", Long.class);
	}





	private ResponseEntity<ProductsDeleteResponse> deleteBundle(Long bundleId, BaseUserEntity user) {
		HttpEntity<?> request =  TestCommons.getHttpEntity("" ,user.getAuthenticationToken());
		ResponseEntity<ProductsDeleteResponse> response =
				template.exchange("/product/bundle?product_id=" + bundleId
						, HttpMethod.DELETE
						, request
						, ProductsDeleteResponse.class);
		return response;
	}
	
	
	
	private ResponseEntity<String> deleteBundleStrReponse(Long bundleId, BaseUserEntity user) {
		HttpEntity<?> request =  TestCommons.getHttpEntity("" , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle?product_id=" + bundleId
						, HttpMethod.DELETE
						, request
						, String.class);
		return response;
	}
	
	
	
	
	@Test
	public void bundleDeleteInvalildAuthNTest() {
		Long bundleId = 200007L;
		
		assertTrue("assert bundle already exists", bundleRepo.existsById(bundleId));
		

		BaseUserEntity user = new EmployeeUserEntity();
		user.setId(0L);
		user.setAuthenticationToken("INVALID TOKIN");
		ResponseEntity<String> response = deleteBundleStrReponse(bundleId, user);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());			
		assertTrue("assert bundle still exists after failed DELETE operation", bundleRepo.existsById(bundleId));
	}
	
	
	
	
	
	@Test
	public void bundleDeleteInvalidAuthZTest() {
		Long bundleId = 200007L;
		
		assertTrue("assert bundle already exists", bundleRepo.existsById(bundleId));
		

		BaseUserEntity user = empUserRepo.getById(68L); //doesn't have delete rights
		ResponseEntity<String> response = deleteBundleStrReponse(bundleId, user);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertTrue("assert bundle still exists after failed DELETE operation", bundleRepo.existsById(bundleId));
	}
	
	
	
	
	@Test
	public void bundleDeleteIdOfNormalProductTest() {
		Long bundleId = 200001L;
		
		assertTrue("assert id is for a normal product", productRepo.existsById(bundleId));
		assertFalse("assert id is NOT for a bundle", bundleRepo.existsById(bundleId));
		

		BaseUserEntity user = empUserRepo.getById(69L); 
		ResponseEntity<String> response = deleteBundleStrReponse(bundleId, user);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());				
		assertTrue("assert product wasn't deleted", productRepo.existsById(bundleId));
	}

	
	
	@Test
	public void addBundleElementTest() {
		Long bundleId = 200008L;
		Long stockId = 400001L;
		
		
		Set<StocksEntity> itemsBefore = helper.getBundleItems(bundleId);
		assertEquals("The bundle should have no elements before",0 , itemsBefore.size());
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("operation", Operation.ADD.getValue());
		reqJson.put("bundle_id", bundleId);
		reqJson.put("stock_id", stockId);
		
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_AMDIN); 
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		Set<StocksEntity> itemsAfter = helper.getBundleItems(bundleId);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotEquals(0, itemsAfter.size());
		assertEquals(1, itemsAfter.size());
		assertEquals(stockId , itemsAfter.stream().findFirst().get().getId());
	}
	
	
	
	
	
	@Test
	public void addBundleElementNoAuthNTest() {
		Long bundleId = 200008L;
		Long stockId = 400001L;
		
		
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("operation", Operation.ADD.getValue());
		reqJson.put("bundle_id", bundleId);
		reqJson.put("stock_id", stockId);
		
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , "NOT EXISTING");
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void addBundleElementNoAuthZTest() {
		Long bundleId = 200008L;
		Long stockId = 400001L;
		
		
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("operation", Operation.ADD.getValue());
		reqJson.put("bundle_id", bundleId);
		reqJson.put("stock_id", stockId);
		
		BaseUserEntity user = empUserRepo.getById(NOT_ORG_ADMIN);
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void addBundleElementAdminOfOtherOrgTest() {
		Long bundleId = 200008L;
		Long stockId = 400001L;
		
		
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("operation", Operation.ADD.getValue());
		reqJson.put("bundle_id", bundleId);
		reqJson.put("stock_id", stockId);
		
		BaseUserEntity user = empUserRepo.getById(OTHER_ORG_ADMIN); 
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void addBundleElementMissingOprTest() {
		Long bundleId = 200008L;
		Long stockId = 400001L;
		
		
		
		JSONObject reqJson = new JSONObject();		
		reqJson.put("bundle_id", bundleId);
		reqJson.put("stock_id", stockId);
		
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_AMDIN); 
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void addBundleElementMissingBundleIdTest() {
		Long stockId = 400001L;
		
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("operation", Operation.ADD.getValue());
		reqJson.put("stock_id", stockId);
		
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_AMDIN); 
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void addBundleElementMissingStockIdTest() {
		Long bundleId = 200008L;
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("operation", Operation.ADD.getValue());
		reqJson.put("bundle_id", bundleId);
		
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_AMDIN); 
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void addBundleElementNonExistingBundleTest() {
		Long bundleId = 2223366L;
		Long stockId = 400001L;
		
		
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("operation", Operation.ADD.getValue());
		reqJson.put("bundle_id", bundleId);
		reqJson.put("stock_id", stockId);
		
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_AMDIN);
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void addBundleElementNonExistingStockTest() {
		Long bundleId = 200008L;
		Long stockId = 8888795473L;
		
		
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("operation", Operation.ADD.getValue());
		reqJson.put("bundle_id", bundleId);
		reqJson.put("stock_id", stockId);
		
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_AMDIN);
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void addBundleElementNotBundleTest() {
		Long bundleId = 200001L;
		Long stockId = 400001L;		
		
		
		assertTrue(productRepo.existsById(bundleId));	
		
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("operation", Operation.ADD.getValue());
		reqJson.put("bundle_id", bundleId);
		reqJson.put("stock_id", stockId);
		
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_AMDIN);
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	public void addBundleElementInvalidOprTest() {
		Long bundleId = 200008L;
		Long stockId = 400001L;		
		
		
		assertTrue(productRepo.existsById(bundleId));	
		
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("operation", Operation.UPDATE.getValue());
		reqJson.put("bundle_id", bundleId);
		reqJson.put("stock_id", stockId);
		
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_AMDIN);
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void deleteBundleElementTest() {
		Long bundleId = 200004L;
		Long stockId = 400001L;
		
		
		Set<StocksEntity> itemsBefore = helper.getBundleItems(bundleId);
		Set<Long> itemsBeforeIdSet = itemsBefore.stream()
												.map(StocksEntity::getId)
												.collect(Collectors.toSet());
		
		assertNotEquals("The bundle should have elements before",0 , itemsBefore.size());
		assertTrue( itemsBeforeIdSet.contains(stockId) );
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("operation", Operation.DELETE.getValue());
		reqJson.put("bundle_id", bundleId);
		reqJson.put("stock_id", stockId);
		
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_AMDIN); 
		
		HttpEntity<?> request =  TestCommons.getHttpEntity(reqJson.toString() , user.getAuthenticationToken());
		ResponseEntity<String> response = 
				template.exchange("/product/bundle/element"
						, HttpMethod.POST
						, request
						, String.class);
		
		Set<StocksEntity> itemsAfter = helper.getBundleItems(bundleId);
		Set<Long> itemsAfterIdSet = itemsAfter.stream()
											.map(StocksEntity::getId)
											.collect(Collectors.toSet());
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue( itemsAfter.size() < itemsBefore.size() );		
		assertFalse( itemsAfterIdSet.contains(stockId) );
	}
}
