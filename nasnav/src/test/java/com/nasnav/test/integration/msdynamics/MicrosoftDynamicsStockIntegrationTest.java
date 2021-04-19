package com.nasnav.test.integration.msdynamics;

import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.service.OrderService;
import com.nasnav.service.SecurityService;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.nasnav.enumerations.PaymentStatus.PAID;
import static com.nasnav.enumerations.TransactionCurrency.EGP;
import static com.nasnav.test.commons.TestCommons.json;




//TODO: this should be added again when the cart checkout logic is complete and it does get the external stocks.
	//the cart checkout method will have to replace [orderService.validateOrderIdsForCheckOut(asList(orderId));]

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
//@PropertySource("classpath:test.database.properties")
//@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_Test_Data_Insert.sql"})
//@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
//@DirtiesContext
public class MicrosoftDynamicsStockIntegrationTest {
	private static final String MOCK_SERVER_URL = "http://127.0.0.1";
	private static final String SERVER_URL = MOCK_SERVER_URL;
//	private static final String SERVER_URL = MS_SERVER_URL;
	private static final boolean usingMockServer = MOCK_SERVER_URL == SERVER_URL;
	
	
	@Autowired
	private IntegrationTestCommon testCommons;
	
	
	@Autowired
	private IntegrationService integrationService;
	
	
	@Autowired
	private PaymentsRepository paymentRepo;
	
	@Autowired
	private OrdersRepository orderRepo;
	
	@Autowired
	private OrderService orderService;

	@MockBean
	private SecurityService securityService;
	
	@Autowired
	private StockRepository stockRepo;
	
	
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
	
	
	
//TODO: this should be added again when the cart checkout logic is complete and it does get the external stocks.
	//the cart checkout method will have to replace [orderService.validateOrderIdsForCheckOut(asList(orderId));]
//	@Test
//	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_order_create_test_data.sql"})
//	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
//	public void createOrderWithNoExtStockTest() throws Throwable {		
//		Mockito.when(securityService.getCurrentUserOrganizationId()).thenReturn(99001L);
//		
//		Long stockId = 60003L;
//		StocksEntity stockBefore = stockRepo.findById(stockId).get();
//		assertNotEquals(0, stockBefore.getQuantity().intValue());
//		//--------------------------------------
//		Long orderId = 330033L;
//		OrdersEntity order = orderRepo.findById(orderId).get();
//		createDummyPayment(order);
//		
//		boolean isThrown = false;
//		try {
//			orderService.validateOrderIdsForCheckOut(asList(orderId));
//		}catch(StockValidationException e) {
//			isThrown = true;
//		}
//		
//		//--------------------------------------
//		assertTrue(isThrown);
//		
//		StocksEntity stockAfter = stockRepo.findById(stockId).get();
//		assertEquals(0, stockAfter.getQuantity().intValue());
//	}
	
	
	
	
	private PaymentEntity createDummyPayment(OrdersEntity order) {
		
		PaymentEntity payment = new PaymentEntity();
		JSONObject paymentObj = 
				json()
				.put("what_is_this?", "dummy_payment_obj");
		
		payment.setOperator("UPG");
		payment.setUid("MLB-<MerchantReference>");
		payment.setExecuted(new Date());
		payment.setObject(paymentObj.toString());
		payment.setAmount(order.getAmount());
		payment.setCurrency(EGP);
		payment.setStatus(PAID);
		payment.setUserId(order.getUserId());
		
		payment= paymentRepo.saveAndFlush(payment);
		order.setPaymentEntity(payment);
		orderRepo.saveAndFlush(order);
		return payment;
	}


	
	
	//TODO: this should be added again when the cart checkout logic is complete and it does get the external stocks.
		//the cart checkout method will have to replace [orderService.validateOrderIdsForCheckOut(asList(orderId));]
//	@Test
//	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_order_create_test_data.sql"})
//	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
//	public void createOrderWithDelayedExtStockTest() throws Throwable {
//		IntegrationServiceImpl.STOCK_REQUEST_TIMEOUT = GET_BY_ID_DELAY - 1;
//		Mockito.when(securityService.getCurrentUserOrganizationId()).thenReturn(99001L);
//		
//		Long stockId = 60002L;
//		StocksEntity stockBefore = stockRepo.findById(stockId).get();
//		assertNotEquals(0, stockBefore.getQuantity().intValue());
//		//--------------------------------------
//		Long orderId = 330034L;
//		OrdersEntity order = orderRepo.findById(orderId).get();
//		createDummyPayment(order);
//		
//		orderService.validateOrderIdsForCheckOut(asList(orderId));
//		
//		assertTrue("The validation shouldn't throw an exception",true);
//		//--------------------------------------
//		Thread.sleep(IntegrationServiceImpl.STOCK_REQUEST_TIMEOUT*1000 + 200);
//		//--------------------------------------
//		StocksEntity stockAfter = stockRepo.findById(stockId).get();
//		assertEquals("if updating the stock from external system timeout, the stock will not be updated"
//				, stockBefore.getQuantity(), stockAfter.getQuantity());
//	}
	
	
	
	
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
