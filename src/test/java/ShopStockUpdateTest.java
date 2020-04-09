import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.NavBox;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.test.helpers.TestHelper;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Stock_Update_API_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ShopStockUpdateTest {
	
	private static final Long TEST_SHOP_ID = 100001L;
	
	private static final Long TEST_VARIANT_ID = 310001L;

	private static final long VALID_ORG_MANAGER = 69L;
	
	private static final long OTHER_VALID_ORG_MANAGER = 72L;

	private static final long OTHER_ORG_MANAGER = 70L;

	private static final long NOT_ORG_MANAGER = 68L;

	private static final Long OTHER_STORE_MANAGER = 71L;

	private static final Long TEST_EXISTING_STOCK_VARIANT_ID = 310011l;

	private static final Long TEST_EXISTING_STOCK_ID = 400001L;


	@Autowired
	private TestRestTemplate template;


	@Autowired
	private EmployeeUserRepository empUserRepo;
	
	
	@Autowired
	private StockRepository stockRepo;
	
	
	@Autowired
	private TestHelper helper;

	@Test
	public void createStockNoAuthNTest() throws JsonProcessingException{
		
		JSONObject updateReq = createStockUpdateReq();
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , "non-existing-token");
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);		
		
		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	public void createStockUnAuthZTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(NOT_ORG_MANAGER); //this user is not an organization admin
		
		JSONObject updateReq = createStockUpdateReq();
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void createStockOtherOrgManagerTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(OTHER_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void createStockOtherShopManagerTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(OTHER_STORE_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	@Test
	public void createStockSameOrgMgrButOtherShopMgrTest() throws JsonProcessingException{
		//This user is Organization manager, but also a shop manager for another shop
		BaseUserEntity user = empUserRepo.getById(OTHER_VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(OK, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void createStockMissingShopIdTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		updateReq.remove("shop_id");
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void createStockMissingVariantIdTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		updateReq.remove("variant_id");
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void createStockMissingQuantityAndPriceTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		updateReq.remove("quantity");
		updateReq.remove("price");
		updateReq.remove("currency");
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void createStockMissingPriceTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		updateReq.remove("quantity");
		updateReq.remove("price");
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void createStockMissingCurrencyTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		updateReq.remove("quantity");
		updateReq.remove("currency");
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	@Test
	public void createStockMissingQuntityOnlyTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		updateReq.remove("quantity");
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(OK, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void createStockMissingPriceAndCurrecnyOnlyTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		updateReq.remove("price");
		updateReq.remove("currency");
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(OK, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void createStockInvalidCurrencyTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		updateReq.put("currency", -140);
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void createStockInvalidPriceTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		updateReq.put("price", -140);
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void createStockNonExistingShopIdTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		updateReq.put("shop_id", 1111111L);
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	public void createStockNonExistingVariantIdTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		updateReq.put("variant_id", 1111111L);
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}

	
	
	
	
	
	@Test
	public void createStockTest() throws JsonProcessingException{		
		
		long countBefore = stockRepo.countByProductVariantsEntity_Id(TEST_VARIANT_ID);
		assertEquals("No stock should exist for the test product variant", 0L, countBefore);
		
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(OK, response.getStatusCode());
		
		JSONObject body = new JSONObject(response.getBody());
		assertTrue(body.has("stock_id"));
		
		Long stockId = body.getLong("stock_id"); 		
		StocksEntity saved = helper.getStockFullData(stockId);
		
		assertEquals(updateReq.getLong("shop_id") 		, saved.getShopsEntity().getId().longValue() );
		assertEquals(updateReq.getLong("variant_id") 	, saved.getProductVariantsEntity().getId().longValue() );
		assertEquals(updateReq.getInt("quantity") 		, saved.getQuantity().intValue() );		
		assertEquals(updateReq.getInt("currency") 		, saved.getCurrency().getValue() );
		assertTrue(updateReq.getBigDecimal("price").compareTo( saved.getPrice() ) == 0 );
	}
	
	
	
	
	@Test
	public void updateStockTest() throws JsonProcessingException{		
		
		StocksEntity stockBefore = helper.getStockFullData(TEST_EXISTING_STOCK_ID);
		assertNotNull("stock should exist for the test product variant", stockBefore);
		
		BaseUserEntity user = empUserRepo.getById(VALID_ORG_MANAGER); 
		
		JSONObject updateReq = createStockUpdateReq();	//only update quantity
		updateReq.put("variant_id", TEST_EXISTING_STOCK_VARIANT_ID);
		updateReq.remove("price");	
		updateReq.remove("currency");
		
		HttpEntity<?> request =  getHttpEntity(updateReq.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, POST
						, request
						, String.class);	
		
		assertEquals(OK, response.getStatusCode());
		
		JSONObject body = new JSONObject(response.getBody());
		assertTrue(body.has("stock_id"));
		
		Long stockId = body.getLong("stock_id"); 
		StocksEntity saved = helper.getStockFullData(stockId);
		
		assertEquals(stockBefore.getShopsEntity().getId() 				, saved.getShopsEntity().getId() );
		assertEquals(stockBefore.getProductVariantsEntity().getId() 	, saved.getProductVariantsEntity().getId() );
		assertEquals(updateReq.getInt("quantity") 						, saved.getQuantity().intValue() );		
		assertEquals(stockBefore.getCurrency() 							, saved.getCurrency() );
		assertTrue(stockBefore.getPrice().compareTo( saved.getPrice() ) == 0 );
	}


	
	
	
	private JSONObject createStockUpdateReq() {
		JSONObject json = new JSONObject();
		
		json.put("shop_id", TEST_SHOP_ID);
		json.put("variant_id", TEST_VARIANT_ID);
		json.put("price", 10.5);
		json.put("currency", 1);
		json.put("quantity", 101);
		
		return json;
	}
}
