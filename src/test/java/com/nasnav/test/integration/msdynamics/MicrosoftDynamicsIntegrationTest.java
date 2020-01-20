package com.nasnav.test.integration.msdynamics;

import static com.nasnav.enumerations.OrderStatus.CLIENT_CONFIRMED;
import static com.nasnav.enumerations.OrderStatus.NEW;
import static com.nasnav.enumerations.PaymentStatus.PAID;
import static com.nasnav.enumerations.TransactionCurrency.EGP;
import static com.nasnav.integration.enums.MappingType.ORDER;
import static com.nasnav.integration.enums.MappingType.PAYMENT;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static com.nasnav.test.commons.TestCommons.jsonArray;
import static com.nasnav.test.commons.TestCommons.readResource;
import static com.nasnav.test.integration.msdynamics.IntegrationTestCommon.DUMMY_PAYMENT_ID;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.verify.VerificationTimes.exactly;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.mockserver.verify.VerificationTimes;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.IntegrationMappingRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.dto.UserDTOs.UserRegistrationObject;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.IntegrationServiceImpl;
import com.nasnav.persistence.IntegrationMappingEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.OrderResponse;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.model.Item;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
@DirtiesContext
public class MicrosoftDynamicsIntegrationTest {
	
	private static final long DUMMY_ORDER = 430033L;
	@SuppressWarnings("unused")
	private static final String MS_SERVER_URL = "http://41.39.128.74";
	private static final String MOCK_SERVER_URL = "http://127.0.0.1";
	private static final String SERVER_URL = MOCK_SERVER_URL;
//	private static final String SERVER_URL = MS_SERVER_URL;
	private static final boolean usingMockServer = MOCK_SERVER_URL == SERVER_URL;
	
	
	private static final Long ORG_ID = 99001L;
	private static final String USER_MAPPING = "CUSTOMER";
	
	
	@Value("classpath:/json/ms_dynamics_integratoin_test/expected_order_request.json")
	private Resource orderRequest;
	
	@Value("classpath:/json/ms_dynamics_integratoin_test/get_stores_response.json")
	private Resource storesJson;
	
	
	@Value("classpath:/json/ms_dynamics_integratoin_test/get_products_response_2.json")
	private Resource productsJson2;
	
	
	@Autowired
	private IntegrationService integrationService;
	
	
	@Autowired
	private IntegrationTestCommon testCommons;
	
	
	@Autowired
	private UserRepository userRepo;
	
	
	@Autowired
	private IntegrationMappingRepository mappingRepo;
	
	
	@Autowired
	private ShopsRepository shopsRepo;
	
	@Autowired
    private TestRestTemplate template;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ProductVariantsRepository variantRepo;
	
	@Autowired
	private BrandsRepository brandRepo;
	
	
	@Autowired
	private PaymentsRepository paymentRepo;
	
	@Autowired
	private OrdersRepository orderRepo;
	
	
	@Autowired
	private IntegrationTestHelper testHelper;
	
	 @Rule
	 public MockServerRule mockServerRule = new MockServerRule(this);
	    
	
	
	@Before
	public void init() throws Exception {			
		String serverFullUrl = SERVER_URL;
		if(usingMockServer) {
			serverFullUrl = testCommons.initFortuneMockServer(mockServerRule);
		}
		
		registerIntegrationModule(serverFullUrl);
	}
	
	
	
	
	
	
	@Test
	public void createCustomerIntegrationTest() throws InterruptedException {
		
		assertEquals("no users should exists", 0L, userRepo.count());
		
		//create customer
		String email = registerNasnavCustomer();
		//------------------------------------------------
		
		//test customer was created
		assertEquals("no users should exists", 1L, userRepo.count());
		UserEntity user = userRepo.findAll().get(0);
		
		assertEquals( email, user.getEmail());
		//------------------------------------------------
		//wait for the integration event to be handled.
		//can't use concurrentunit.Waiter class, the response is served by the MockServer
		Thread.sleep(7000);
		
		//------------------------------------------------
		//test the mock api was called
		if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("PUT")
				        .withPath("/api/customer"),
				      VerificationTimes.exactly(1)
				    );
		}
		//------------------------------------------------
		//test the integration mapping was created
		Optional<IntegrationMappingEntity> mapping = 
				mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(
													ORG_ID, USER_MAPPING, user.getId().toString());
		
		assertTrue(mapping.isPresent());
		if(usingMockServer) {
			assertEquals(testCommons.getDummyCustomerExtId(), mapping.get().getRemoteValue());
		}
	}
	
	
	
	
	
	@Test
	public void importStoresTest() throws Throwable {

		long countBefore = shopsRepo.count();
		
		//------------------------------------------------		
		//push shop import event and wait for it
		HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<String> response = template.exchange("/integration/import/shops", HttpMethod.GET, request, String.class);
        ObjectMapper mapper = new ObjectMapper();       
        List<Long> importedShops = mapper.readValue(response.getBody().getBytes(), new TypeReference<List<Long>>(){});
		
		//------------------------------------------------
		//test the mock api was called
		if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/api/stores"),
				      VerificationTimes.exactly(1)
				    );
		}
		//------------------------------------------------
		//test the imported shops were created
		JSONArray extShopsJson = getExpectedShopsJson();
		
		long countAfter = shopsRepo.count();
					
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("stores were imported", extShopsJson.length(), countAfter - countBefore);
		assertTrue("all imported stores id's have integration mapping" , allShopIdsHaveMapping(importedShops));
	}






	private JSONArray getExpectedShopsJson() throws IOException {
		String shopsResponse = readResource(storesJson);
		JSONArray extShopsJson = new JSONObject(shopsResponse)
										.getJSONArray("results")
										.getJSONObject(0)
										.getJSONArray("shops");
		return extShopsJson;
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_products_import_test_data.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void importProductsTest() throws Throwable {
		int count = 7;
		long countProductsBefore = productRepo.count();
		long countShopsBefore = shopsRepo.count();
		
		//------------------------------------------------		
		//call product import api
		JSONObject requestJson = 
				json()
					.put("dryrun", false)
					.put("update_product", true)
					.put("update_stocks", true)
					.put("currency", 1)
					.put("encoding", "UTF-8")
					.put("page_count", count);
		
		HttpEntity<Object> request = getHttpEntity(requestJson.toString(), "hijkllm");
        ResponseEntity<Integer> response = template.exchange("/integration/import/products", HttpMethod.POST, request, Integer.class);       
        		
		//------------------------------------------------
		//test the mock api was called
		if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/api/products/\\d+/\\d+"),
				      VerificationTimes.exactly(1)
				    );
		}
		//------------------------------------------------
		//test imported brands were created
		//test the imported products were created
		
		long countProductsAfter = productRepo.count();
		long countShopsAfter = shopsRepo.count();
		JSONArray extShopsJson = getExpectedShopsJson();
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotEquals("products were imported", 0L, countProductsAfter - countProductsBefore);
		if(usingMockServer) {
			assertEquals("shops were imported", extShopsJson.length() - countShopsBefore, countShopsAfter - countShopsBefore);
			assertEquals("assert brands were imported", 3L, brandRepo.count());
			assertTrue("all imported products have integration mapping" , allProductHaveMapping());
			assertEquals("check number of remaining pages to import", 0, response.getBody().intValue());
		}
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_get_stock_test_data.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void getVariantExternalStockTest() throws Throwable {
		Long VARIANT_ID = 310001L;
		Long SHOP_ID = 50001L;
		String url = format("/test/integration/get_stock?variant_id=%d&shop_id=%d", VARIANT_ID, SHOP_ID);
		
		Integer stkQty = template.postForEntity(url, getHttpEntity("hijkllm"), Integer.class).getBody();
		if(usingMockServer) {
			assertEquals(101, stkQty.intValue());
		}
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_get_stock_test_data.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void getVariantExternalStockFailureTest() throws Throwable {
		Long VARIANT_ID = 310003L;
		Long SHOP_ID = 55555L;
		String url = format("/test/integration/get_stock?variant_id=%d&shop_id=%d", VARIANT_ID, SHOP_ID);
		
		ResponseEntity<String> response = template.postForEntity(url, getHttpEntity("hijkllm"), String.class);
		if(usingMockServer) {
			assertEquals(
					"Calling the mockserver with the given parameters will return response 500"
					+ ", so , the local stock on nasnav is returned as a fallback value."
					, 88, Integer.valueOf(response.getBody()).intValue());
		}
	}

	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_get_stock_test_data.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void getVariantWithNoMappingExternalStockTest() throws Throwable {
		Long VARIANT_ID = 310002L;
		Long SHOP_ID = 50001L;
		String url = format("/test/integration/get_stock?variant_id=%d&shop_id=%d", VARIANT_ID, SHOP_ID);
		
		Integer stkQty = template.postForEntity(url, getHttpEntity("hijkllm"), Integer.class).getBody();
		if(usingMockServer) {
			assertEquals(55, stkQty.intValue());
		}
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_order_create_test_data.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void createOrderTest() throws Throwable {
		//create order
		String token = "123eerd";
		
		Long orderId = createNewOrder(token); 
		confirmOrder(token, orderId);
		//---------------------------------------------------------------		
		Thread.sleep(3000);
		//---------------------------------------------------------------		
		assertOrderIntegration(orderId); 					
	}

	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_pay_create_test_data.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void createPaymentTest() throws Throwable {
		PaymentEntity payment = createDummyPayment(DUMMY_ORDER);
		//---------------------------------------------------------------		
		Thread.sleep(5000);
		//---------------------------------------------------------------
		assertPaymentIntegration(payment);
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_pay_create_test_data.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void createPaymentAlreadySentTest() throws Throwable {
		Long orderId = 430033L;
		PaymentEntity payment = testHelper.createDummyPayment(orderId);
		payment = testHelper.updatePayment(payment.getId());
		//---------------------------------------------------------------		
		Thread.sleep(5000);
		//---------------------------------------------------------------
		//The integration event should be issued once!
		assertPaymentIntegration(payment);
	}
	
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_pay_create_test_data.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void createPaymentBeforeConfirmingOrderTest() throws Throwable {
		long oldTimeout = IntegrationServiceImpl.REQUEST_TIMEOUT_SEC;
		IntegrationServiceImpl.REQUEST_TIMEOUT_SEC = 30L;		
		
		//create order
		String token = "123eerd";
		
		Long orderId = createNewOrder(token);
		PaymentEntity payment = createDummyPayment(orderId);	
		Thread.sleep(2000);
		confirmOrder(token, orderId);		
		//---------------------------------------------------------------		
		Thread.sleep(4000);
		//---------------------------------------------------------------
		assertPaymentIntegration(payment, "UNR19-050000");
		assertOrderIntegration(orderId); 
		
		IntegrationServiceImpl.REQUEST_TIMEOUT_SEC = oldTimeout;
	}






	private void assertPaymentIntegration(PaymentEntity payment) throws AssertionError {
		String expectedBody = getPaymentApiRequestExpectedBody();
		if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("PUT")
				        .withPath("/api/Payment")
				        .withBody(json(expectedBody))
				      ,exactly(1)
				    );
		}
		
		IntegrationMappingEntity paymentMapping = 
				mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(ORG_ID, PAYMENT.getValue(), payment.getId().toString())
							.orElse(null);
		assertNotNull(paymentMapping);
		assertEquals(DUMMY_PAYMENT_ID, paymentMapping.getRemoteValue());
	}
	
	
	
	
	private void assertPaymentIntegration(PaymentEntity payment, String externalOrderId) throws AssertionError {
		String expectedBody = getPaymentApiRequestExpectedBody(externalOrderId);
		if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("PUT")
				        .withPath("/api/Payment")
				        .withBody(json(expectedBody))
				      ,exactly(1)
				    );
		}
		
		IntegrationMappingEntity paymentMapping = 
				mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(ORG_ID, PAYMENT.getValue(), payment.getId().toString())
							.orElse(null);
		assertNotNull(paymentMapping);
		assertEquals(DUMMY_PAYMENT_ID, paymentMapping.getRemoteValue());
	}






	private String getPaymentApiRequestExpectedBody() {
		return	json()
				.put("SalesId", "un7782885")
				.put("PaymDet", 
						jsonArray()
						.put(
							json()
							.put("SalesId", "un7782885")
							.put("Amount", 600)
							.put("PaymentMethod", "Credit_CHE")
						  )
						
				).toString();
	}
	
	
	
	
	
	
	private String getPaymentApiRequestExpectedBody(String externalOrderId) {
		return	json()
				.put("SalesId", externalOrderId)
				.put("PaymDet", 
						jsonArray()
						.put(
							json()
							.put("SalesId", externalOrderId)
							.put("Amount", 600)
							.put("PaymentMethod", "Credit_CHE")
						  )
						
				).toString();
	}






	private PaymentEntity createDummyPayment(Long orderId) {
		
		OrdersEntity order = orderRepo.findById(orderId).get();
		PaymentEntity payment = new PaymentEntity();
		JSONObject paymentObj = 
				json()
				.put("what_is_this?", "dummy_payment_obj");
		
		payment.setOperator("UPG");
		payment.setOrdersEntity(order);
		payment.setUid("MLB-<MerchantReference>");
		payment.setExecuted(new Date());
		payment.setObject(paymentObj.toString());
		payment.setAmount(new BigDecimal("600"));
		payment.setCurrency(EGP);
		payment.setStatus(PAID);
		
		payment= paymentRepo.save(payment);
		return payment;
	}





	private void assertOrderIntegration(Long orderId) throws IOException, AssertionError {
		//check the api was called with the expected request body
		String expectedExtOrderRequest = readResource(orderRequest);
		if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("PUT")
				        .withPath("/api/salesorder")
				        .withBody(json(expectedExtOrderRequest))
				        ,
				      VerificationTimes.exactly(1)
				    );
		}
		
		//---------------------------------------------------------------
		//validate an integration mapping was created
		
		Optional<IntegrationMappingEntity> orderMappingAfterOrderConfirm = 
				mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(ORG_ID, ORDER.getValue(), orderId.toString());
		assertTrue(orderMappingAfterOrderConfirm.isPresent());
	}






	private void confirmOrder(String token, Long orderId) {
		JSONObject updateRequest = createOrderRequestWithBasketItems(CLIENT_CONFIRMED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity( updateRequest.toString(), token)
										, String.class);
		
		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
	}






	private Long createNewOrder(String token) {
		Long stockId = 60001L;
		Integer orderQuantity = 5;
		
		//---------------------------------------------------------------
		JSONObject request = createOrderRequestWithBasketItems(NEW, item(stockId, orderQuantity));
		ResponseEntity<OrderResponse> response = 
				template.postForEntity("/order/update"
										, getHttpEntity( request.toString(), token)
										, OrderResponse.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Long orderId = response.getBody().getOrderId();
		Optional<IntegrationMappingEntity> orderMappingAfterOrderCreation = 
				mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(ORG_ID, ORDER.getValue(), orderId.toString());
		assertFalse(orderMappingAfterOrderCreation.isPresent());
		return orderId;
	}



	
	
	private Item item(Long stockId, Integer quantity) {
		return new Item(stockId, quantity);
	}
	
	
	
	
	
	private JSONObject createOrderRequestWithBasketItems(OrderStatus status, Item... items) {
		JSONArray basket = createBasket( items);
		
		JSONObject request = new JSONObject();
		request.put("status", status.name());
		request.put("basket", basket);
		return request;
	}
	
	
	
	
	private JSONArray createBasket(Item...items) {
		return new JSONArray( 
				asList(items)
					.stream()
					.map(Item::toJsonObject)				
					.collect(Collectors.toList())
				);
	}
	
	
	


	private boolean allProductHaveMapping() {
		return variantRepo
				.findByOrganizationId(ORG_ID)
				.stream()
				.map(ProductVariantsEntity::getId)
				.map(id -> id.toString())
				.map(id -> mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(ORG_ID, "PRODUCT_VARIANT", id))
				.allMatch(Optional::isPresent);			
	}






	private Boolean allShopIdsHaveMapping(List<Long> importedShops) {
		Boolean allShopsHaveMapping = 
				importedShops
					.stream()
					.map(id -> id.toString())
					.map(id -> mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(ORG_ID, "SHOP", id))
					.allMatch(Optional::isPresent);
		return allShopsHaveMapping;
	}
	
	
	


	private String registerNasnavCustomer() {
		UserRegistrationObject regObj = new UserRegistrationObject();
		String email = "nasnav_awesome_test_account@nasnav.com";
		regObj.email = email;
		regObj.name = "Nasnav";
		regObj.orgId = ORG_ID;
		
		
		UserEntity user = UserEntity.registerUser(regObj);
		user.setPhoneNumber("000111444");
//		user.setAddress("nasnav st.");
//		user.setAddressCity("Cairo");
//		user.setAddressCountry("Egypt");
		userRepo.saveAndFlush(user);
		
		return email;
	}
	
	
	
	

	private void registerIntegrationModule(String serverFullUrl) throws BusinessException {
		Map<String,String> params = new HashMap<>();
		params.put("SERVER_URL", serverFullUrl);
		
		OrganizationIntegrationInfoDTO integrationInfo = new OrganizationIntegrationInfoDTO();
		integrationInfo.setIntegrationModule("com.nasnav.integration.microsoftdynamics.MsDynamicsIntegrationModule");
		integrationInfo.setMaxRequestRate(1);
		integrationInfo.setOrganizationId(99001L);
		integrationInfo.setIntegrationParameters(params);
		
		integrationService.registerIntegrationModule(integrationInfo);
	}
}
