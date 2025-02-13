package com.nasnav.test.integration.msdynamics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.*;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.dto.UserDTOs.UserRegistrationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.persistence.IntegrationMappingEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.OrderService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.nasnav.test.commons.TestCommons.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.mockserver.model.HttpRequest.request;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@RunWith(SpringRunner.class)
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class MicrosoftDynamicsIntegrationTest extends AbstractTestWithTempBaseDir {
	
	@SuppressWarnings("unused")
	private static final String NASNAV_ADMIN_TOKEN = "abcdefg";
	private static final String MS_SERVER_URL = "http://41.39.128.74";
	private static final String MOCK_SERVER_URL = "http://127.0.0.1";
	private static final String SERVER_URL = MOCK_SERVER_URL;
//	private static final String SERVER_URL = MS_SERVER_URL;
	private static final boolean usingMockServer = MOCK_SERVER_URL == SERVER_URL;
	
	
	private static final Long ORG_ID = 99001L;
	private static final String USER_MAPPING = "CUSTOMER";
	private static final String EXISTING_SHOP_ID = "502";
	
	
	@Value("classpath:/json/ms_dynamics_integratoin_test/expected_order_request.json")
	private Resource orderRequest;

	@Value("classpath:/json/ms_dynamics_integratoin_test/expected_order_request_2.json")
	private Resource orderRequest2;
	
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
	private OrderService orderService;

	@Autowired
	private TagsRepository tagRepo;

	@Autowired
	private AddressRepository addressRepo;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this);
	    
	private String serverFullUrl;

	@Before
	public void init() throws Exception {
		serverFullUrl = SERVER_URL;
		if(usingMockServer) {
			serverFullUrl = testCommons.initFortuneMockServer(mockServerRule);
		}
		integrationService.clearAllIntegrationModules();
		registerIntegrationModule(serverFullUrl);
	}
	
	
	
	
	
	// not sure why this required a new context
	@Test
	public void createCustomerIntegrationTest() throws InterruptedException {
		//---------------------------------------------------------------
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
		Thread.sleep(5000);
		
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
        ResponseEntity<String> response = template.exchange("/integration/import/products", POST, request, String.class);       
        
        assertEquals(OK, response.getStatusCode());
		//------------------------------------------------
		//test the mock api's was called
		if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/api/products/\\d+/\\d+"),
				      VerificationTimes.exactly(1)
				    );
			
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/api/categories"),
				      VerificationTimes.exactly(1)
				    );
		}
		//------------------------------------------------
		//test imported brands were created
		//test the imported products were created
		
		long countProductsAfter = productRepo.count();
		long countShopsAfter = shopsRepo.count();
		JSONArray extShopsJson = getExpectedShopsJson();
		
		assertEquals(OK, response.getStatusCode());
		assertNotEquals("products were imported", 0L, countProductsAfter - countProductsBefore);
		if(usingMockServer) {
			assertEquals("shops were imported", extShopsJson.length() - countShopsBefore, countShopsAfter - countShopsBefore);
			assertEquals("assert brands were imported", 3L, brandRepo.count());
			assertTrue("all imported products have integration mapping" , allProductHaveMapping());
			assertEquals("check number of remaining pages to import", 0, Integer.valueOf(response.getBody()).intValue());
			assertNewTagsImported();
		}
	}
	
	
	
	
	
	
	private void assertNewTagsImported() {
		List<String> existingTags = 
				StreamSupport
				.stream(tagRepo.findAll().spliterator(), false)
				.map(TagsEntity::getName)
				.collect(toList());
		assertTrue("Brands are imported as tags as well", existingTags.contains("Cybele"));
		assertTrue("All the products are given the tag brand", existingTags.contains("All Products"));
		assertTrue("Categories parents are imported as tags", existingTags.contains("Web Sits"));
	}






	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_get_stock_test_data.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void getVariantExternalStockTest() throws Throwable {
//		registerIntegrationModulesViaApi();
		//---------------------------------------------------------------
		Long VARIANT_ID = 310001L;
		Long SHOP_ID = 50001L;
		String url = format("/test/integration/get_stock?variant_id=%d&shop_id=%d", VARIANT_ID, SHOP_ID);
		
		ResponseEntity<String> response = template.postForEntity(url, getHttpEntity("hijkllm"), String.class);
		Integer stkQty = Integer.valueOf(response.getBody());
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
			assertEquals(OK, response.getStatusCode());
			assertEquals(
					"Calling the mockserver with the given parameters will return response 500"
					+ ", so , the local stock on nasnav is returned as a fallback value."
					, -1, Integer.valueOf(response.getBody()).intValue());
		}
	}


	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_get_stock_test_data.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void getVariantWithNoMappingExternalStockTest() throws Throwable {
		Long VARIANT_ID = 310002L;
		Long SHOP_ID = 50001L;
		String url = format("/test/integration/get_stock?variant_id=%d&shop_id=%d", VARIANT_ID, SHOP_ID);
		
		
		ResponseEntity<String> stkQty = template.postForEntity(url, getHttpEntity("hijkllm"), String.class);
		if(usingMockServer) {
			assertEquals(NOT_ACCEPTABLE, stkQty.getStatusCode());
		}
	}


	
	String getPaymentApiRequestExpectedBody(String externalOrderId, BigDecimal amount) {
		return	json()
				.put("SalesId", externalOrderId)
				.put("PaymDet", 
						jsonArray()
						.put(
							json()
							.put("SalesId", externalOrderId)
							.put("Amount", new DecimalFormat("#0.00").format(amount))
							.put("PaymentMethod", "Credit_CHE")
						  )
				).toString();
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
		user = userRepo.saveAndFlush(user);

		user.insertUserAddress(addressRepo.findById(12300001L).get());
		userRepo.saveAndFlush(user);

		return email;
	}
	
	
	
	

	private void registerIntegrationModule(String serverFullUrl) throws BusinessException {
		OrganizationIntegrationInfoDTO integrationInfo = getMsDynamicsIntegrationInfo(serverFullUrl);
		integrationService.registerIntegrationModule(integrationInfo);
	}



	private OrganizationIntegrationInfoDTO getMsDynamicsIntegrationInfo(String serverFullUrl) {
		Map<String,String> params = new HashMap<>();
		params.put("SERVER_URL", serverFullUrl);

		OrganizationIntegrationInfoDTO integrationInfo = new OrganizationIntegrationInfoDTO();
		integrationInfo.setIntegrationModule("com.nasnav.integration.microsoftdynamics.MsDynamicsIntegrationModule");
		integrationInfo.setMaxRequestRate(100);
		integrationInfo.setOrganizationId(99001L);
		integrationInfo.setIntegrationParameters(params);
		return integrationInfo;
	}


	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_Test_Data_Insert_Existing_Shops.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void testImportShopsWithExistingShopName() throws Throwable {

		long countBefore = shopsRepo.count();
		
		//------------------------------------------------		
		//push shop import event and wait for it
		HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<String> response = template.exchange("/integration/import/shops", GET, request, String.class);
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
					
		assertEquals(OK, response.getStatusCode());
		assertEquals("new stores were imported except for the existing one", extShopsJson.length() - 1, countAfter - countBefore);
		assertTrue("all imported stores id's have integration mapping" , allShopIdsHaveMapping(importedShops));
		assertExistingShopHadMapping();
	}



	private void assertExistingShopHadMapping() {
		Optional<IntegrationMappingEntity> mapping = mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(ORG_ID, "SHOP", EXISTING_SHOP_ID);
		assertEquals("exsting shop had mapping" , "FOoscar", mapping.get().getRemoteValue());
	}
}
