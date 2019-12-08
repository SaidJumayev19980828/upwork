package com.nasnav.test.integration.msdynamics;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.IntegrationMappingRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.dto.UserDTOs.UserRegistrationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.persistence.IntegrationMappingEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class MicrosoftDynamicsIntegrationTest {
	
	private static final String msServerUrl = "http://41.39.128.74";
	private static final String mockServerUrl = "http://127.0.0.1";
	private static final String serverUrl = mockServerUrl;
	
	private static final Long ORG_ID = 99001L;
	private static final String USER_MAPPING = "CUSTOMER";
	
	@Autowired
	private IntegrationService integrationService;
	
	
	@Autowired
	private IntegrationTestCommon testCommons;
	
	
	@Autowired
	private UserService userSrv;
	
	
	@Autowired
	private UserRepository userRepo;
	
	
	@Autowired
	private IntegrationMappingRepository mappingRepo;
	
	
	 @Rule
	 public MockServerRule mockServerRule = new MockServerRule(this);
	    
	
	
	@Before
	public void init() throws Exception {			
		testCommons.initFortuneMockServer(mockServerRule);
		registerIntegrationModule();
	}
	
	
	
	
	
	
	@Test
	public void createCustomerIntegrationTest() {
		
		assertEquals("no users should exists", 0L, userRepo.count());
		
		//create customer
		String email = registerNasnavCustomer();
		//------------------------------------------------
		
		//test customer was created
		assertEquals("no users should exists", 1L, userRepo.count());
		UserEntity user = userRepo.findAll().get(0);
		
		assertEquals( email, user.getEmail());
		//------------------------------------------------
		
		//test the mock api was called
		mockServerRule.getClient().verify(
			      request()
			        .withMethod("PUT")
			        .withPath("/api/customer"),
			      VerificationTimes.exactly(1)
			    );
		//------------------------------------------------
		//test the integration mapping was created
		Optional<IntegrationMappingEntity> mapping = 
				mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(
													ORG_ID, USER_MAPPING, user.getId().toString());
		
		assertEquals(testCommons.getDummyCustomerExtId(), mapping.get().getRemoteValue());
	}






	private String registerNasnavCustomer() {
		UserRegistrationObject regObj = new UserRegistrationObject();
		String email = "nasnav_awesome_test_account@nasnav.com";
		regObj.email = email;
		regObj.name = "Nasnav";
		regObj.orgId = ORG_ID;
		
		userSrv.registerUser(regObj);
		return email;
	}
	
	
	
	

	private void registerIntegrationModule() throws BusinessException {
		Map<String,String> params = new HashMap<>();
		params.put("SERVER_URL", serverUrl);
		
		OrganizationIntegrationInfoDTO integrationInfo = new OrganizationIntegrationInfoDTO();
		integrationInfo.setIntegrationModule("com.nasnav.integration.microsoftdynamics.MsDynamicsIntegrationModule");
		integrationInfo.setMaxRequestRate(1);
		integrationInfo.setOrganizationId(99001L);
		integrationInfo.setIntegrationParameters(params);
		
		integrationService.registerIntegrationModule(integrationInfo);
	}
}
