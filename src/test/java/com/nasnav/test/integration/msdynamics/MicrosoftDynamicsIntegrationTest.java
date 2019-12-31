package com.nasnav.test.integration.msdynamics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static com.nasnav.test.commons.TestCommons.*;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.IntegrationMappingRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.dto.UserDTOs.UserRegistrationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventResult;
import com.nasnav.integration.events.ShopsFetchEvent;
import com.nasnav.integration.events.data.ShopsFetchParam;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.integration.model.IntegratedShop;
import com.nasnav.persistence.IntegrationMappingEntity;
import com.nasnav.persistence.UserEntity;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
@DirtiesContext
public class MicrosoftDynamicsIntegrationTest {
	
	private static final String MS_SERVER_URL = "http://41.39.128.74";
	private static final String MOCK_SERVER_URL = "http://127.0.0.1";
	private static final String SERVER_URL = MOCK_SERVER_URL;
//	private static final String SERVER_URL = MS_SERVER_URL;
	private static final boolean usingMockServer = MOCK_SERVER_URL == SERVER_URL;
	
	
	private static final Long ORG_ID = 99001L;
	private static final String USER_MAPPING = "CUSTOMER";
	
	
	@Value("classpath:/json/ms_dynamics_integratoin_test/get_stores_response.json")
	private Resource storesJson;
	
	
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
	public void importStoresTest() throws InterruptedException, InvalidIntegrationEventException, IOException, JSONException {

		long countBefore = shopsRepo.count();
		assertEquals("no stores should exists", 0L, countBefore);
		
		//------------------------------------------------		
		//push shop import event and wait for it
		AtomicBoolean isEventHandled = new AtomicBoolean(false);
		ShopsFetchEvent importShopEvent = 
				new ShopsFetchEvent(ORG_ID, new ShopsFetchParam(), res -> onShopsImportSuccess(res, isEventHandled) );
		
		integrationService	
				.pushIntegrationEvent(importShopEvent, (e,t) -> assertTrue(false))
				.block(Duration.ofSeconds(20L));
		//------------------------------------------------
		//wait for the integration event to be handled.
		//can't use concurrentunit.Waiter class, the response is served by the MockServer
//		Thread.sleep(7000);
		
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
		String shopsResponse = readResource(storesJson);
		int len = new JSONObject(shopsResponse)
						.getJSONArray("results")
						.length();
		
		long countAfter = shopsRepo.count();
		assertEquals("stores were imported", len, countAfter);
	}
	
	
	
	
	
	
	private void onShopsImportSuccess(EventResult<ShopsFetchParam, List<IntegratedShop>> result, AtomicBoolean isEventHandled) {
		List<IntegratedShop> importedShops = result.getReturnedData();
		System.out.println("Shops Imported!");
		System.out.println("imported shops: " + importedShops);
		isEventHandled.set(true);
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
