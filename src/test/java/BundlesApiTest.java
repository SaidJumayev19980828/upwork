import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

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
import com.nasnav.response.BundleResponse;
import com.nasnav.response.ProductUpdateResponse;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Bundle_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/Bundle_Test_Data_Delete.sql"})
public class BundlesApiTest {
	
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
		Long bundleId = 200004L;
		
		
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
		
		assertEquals(1, bundleRes.getBundles().size());
		
		
		
		
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
	
	
	
	
	@Test	
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
	}
	
	
	
	
	
	@Test
	public void createBundleNoAuthNTest() throws JsonProcessingException{
		
		JSONObject bundle = createNewDummyProduct();
		
		HttpEntity request =  TestCommons.getHttpEntity(bundle.toString() , 0L, "non-existing-token");
		
		ResponseEntity<ProductUpdateResponse> response = 
				template.exchange("/product/bundles"
						, HttpMethod.POST
						, request
						, ProductUpdateResponse.class);		
		
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
		assertEquals(HttpStatus.OK, response.getStatusCode());	
		ProductUpdateResponse body = response.getBody();
		assertTrue( body.isSuccess());
		assertNotEquals(body.getProductId() , Long.valueOf(0L));
		assertTrue(bundleRepo.existsById(body.getProductId()));
	}
	
	
	
	private ResponseEntity<ProductUpdateResponse> postProductData(BaseUserEntity user, JSONObject productJson)
			throws JsonProcessingException {
		
		HttpEntity request =  TestCommons.getHttpEntity(productJson.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<ProductUpdateResponse> response = 
				template.exchange("/product/bundle"
						, HttpMethod.POST
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
		assertEquals(product.get("category_id"), saved.getCategoryId());
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
		Long bundleId = 200007L;
		
		assertTrue("assert bundle already exists", bundleRepo.existsById(bundleId));
		

		BaseUserEntity user = empUserRepo.getById(69L); 
		ResponseEntity<ProductUpdateResponse> response = deleteBundle(bundleId, user);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(bundleId, response.getBody().getProductId());		
		assertFalse("assert bundle doesn't exists", bundleRepo.existsById(bundleId));
	}





	private ResponseEntity<ProductUpdateResponse> deleteBundle(Long bundleId, BaseUserEntity user) {
		HttpEntity request =  TestCommons.getHttpEntity("" , user.getId(), user.getAuthenticationToken());
		ResponseEntity<ProductUpdateResponse> response = 
				template.exchange("/product/bundle?product_id=" + bundleId
						, HttpMethod.DELETE
						, request
						, ProductUpdateResponse.class);
		return response;
	}
	
	
	
	private ResponseEntity<String> deleteBundleStrReponse(Long bundleId, BaseUserEntity user) {
		HttpEntity request =  TestCommons.getHttpEntity("" , user.getId(), user.getAuthenticationToken());
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
	public void bundleDeleteExistsInOrdersTest() {
		Long bundleId = 200007L;
		
		assertTrue("assert bundle already exists", bundleRepo.existsById(bundleId));
		String sql = "select count(*) from public.baskets  b\n" + 
						"left join public.stocks s\n" + 
						"on b.stock_id = s.id\n" + 
						"left join public.products p\n" + 
						"on s.product_id = p.id\n" + 
						"where p.id = " + bundleId;			
		Long count = jdbc.queryForObject(sql, Long.class);
		
		assertNotEquals("assert bundle exists in some orders", 0L, count.longValue());
		assertEquals("assert product count JPQL query works"
				,  count
				,  basketRepo.countByProductId(bundleId));
		

		BaseUserEntity user = empUserRepo.getById(69L); 
		ResponseEntity<String> response = deleteBundleStrReponse(bundleId, user);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());		
		assertTrue("assert bundle still exists after failed DELETE operation", bundleRepo.existsById(bundleId));
	}
	
}
