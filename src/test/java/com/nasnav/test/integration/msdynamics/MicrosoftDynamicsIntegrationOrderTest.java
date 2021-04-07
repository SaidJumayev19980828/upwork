package com.nasnav.test.integration.msdynamics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.IntegrationMappingRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.dto.response.navbox.SubOrder;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.IntegrationMappingEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.service.OrderService;
import com.nasnav.test.commons.TestCommons;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.nasnav.enumerations.PaymentStatus.PAID;
import static com.nasnav.enumerations.TransactionCurrency.EGP;
import static com.nasnav.integration.enums.MappingType.ORDER;
import static com.nasnav.integration.enums.MappingType.PAYMENT;
import static com.nasnav.shipping.services.DummyShippingService.SHOP_ID;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.readResource;
import static org.junit.Assert.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.verify.VerificationTimes.exactly;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/MS_dynamics_integration_order_create_test_data.sql"})
@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class MicrosoftDynamicsIntegrationOrderTest {

    @SuppressWarnings("unused")
    private static final String NASNAV_ADMIN_TOKEN = "abcdefg";
    private static final String MS_SERVER_URL = "http://41.39.128.74";
    private static final String MOCK_SERVER_URL = "http://127.0.0.1";
    private static final String SERVER_URL = MOCK_SERVER_URL;
    //	private static final String SERVER_URL = MS_SERVER_URL;
    private static final boolean usingMockServer = MOCK_SERVER_URL == SERVER_URL;

    private static final Long ORG_ID = 99001L;

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentsRepository paymentRepo;

    private String serverFullUrl;


    @Value("classpath:/json/ms_dynamics_integratoin_test/expected_order_request.json")
    private Resource orderRequest;

    @Value("classpath:/json/ms_dynamics_integratoin_test/expected_order_request_2.json")
    private Resource orderRequest2;

    @Autowired
    private IntegrationMappingRepository mappingRepo;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntegrationTestCommon testCommons;


    @Before
    public void init() throws Exception {
        serverFullUrl = SERVER_URL;
        if(usingMockServer) {
            serverFullUrl = testCommons.initFortuneMockServer(mockServerRule);
        }
    }


    @Test
    public void createOrderTest() throws Throwable {
        registerIntegrationModulesViaApi();
        //---------------------------------------------------------------
        String token = "123eerd";

        Order order = createNewOrder(token);
        PaymentEntity payment = createDummyPayment(order);
        finalizeOrder(token, order.getOrderId());
        //---------------------------------------------------------------
        Thread.sleep(5000);
        //---------------------------------------------------------------
        order
                .getSubOrders()
                .forEach(this::assertOrderIntegration);

        assertPaymentIntegration(order, payment);
    }



    private void assertPaymentIntegration(Order order, PaymentEntity payment) throws AssertionError {
        if(usingMockServer) {
            mockServerRule.getClient().verify(
                    request()
                            .withMethod("PUT")
                            .withPath("/api/Payment")
//				        .withBody(json(expectedBody))		//seems there is a bug in MockServer that makes it parse the request as string instead of JSON
                    ,exactly(order.getSubOrders().size())	//for each sub-order an external payment should be created
            );
        }
        assertPaymentMappingCreated(payment);
    }


    private void registerIntegrationModulesViaApi() {
        //register the module, making sure it is done on the right spring context, as for some reason
        //registering the module directly on the autowired integrationService doesn't work, and modules map isn't updated,
        // despite being in a singleton bean.
        // it is as if the test is being run on totally different spring context, so, to make sure, I will
        //try registering the module using api's
        //create order
        String url = "/integration/module";
        String json = getIntegrationModuleRequestJson();

        HttpEntity<?> request =  getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);

        ResponseEntity<String> deleteResponse =
                template.exchange(url + "?organization_id=99001"
                        , DELETE
                        , request
                        , String.class);
        assertEquals(OK, deleteResponse.getStatusCode());

        ResponseEntity<String> response =
                template.exchange(url
                        , POST
                        , request
                        , String.class);
        assertEquals(OK, response.getStatusCode());
    }




    private void payCodViaApi(Long metaOrderId, String token) {
        //pay cod via API
        String url = "/payment/cod/execute?order_id=" + metaOrderId;
        HttpEntity<?> request =  getHttpEntity("", token);

        ResponseEntity<String> response =
                template.exchange(url
                        , POST
                        , request
                        , String.class);

        assertEquals(OK, response.getStatusCode());
    }



    @Test
    public void createOrderWithCodPaymentTest() throws Throwable {
        registerIntegrationModulesViaApi();
        //---------------------------------------------------------------
        //create order
        String token = "123eerd";

        Order order = createNewOrder(token);
        payCodViaApi(order.getOrderId(), token);
        //---------------------------------------------------------------
        Thread.sleep(5000);
        //---------------------------------------------------------------
        order
            .getSubOrders()
            .forEach(this::assertOrderIntegration);

        assertNoPaymentIntegration();
    }


    private void assertOrderIntegration(SubOrder subOrder){
        //check the api was called with the expected request body
        String expectedExtOrderRequest = getExpectedOrderRequest(subOrder);
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
                mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(
                        ORG_ID, ORDER.getValue(), subOrder.getSubOrderId().toString());
        assertTrue(orderMappingAfterOrderConfirm.isPresent());
    }



    private void finalizeOrder(String token, Long orderId) throws BusinessException {
        orderService.finalizeOrder(orderId);
    }


    private String getExpectedOrderRequest(SubOrder subOrder) {
        Long shopId = subOrder.getShopId();
        return shopId == 50001L ?
                readResource(orderRequest)
                : readResource(orderRequest2);
    }



    private void assertNoPaymentIntegration() {
        if(usingMockServer) {
            mockServerRule.getClient().verify(
                    request()
                            .withMethod("PUT")
                            .withPath("/api/Payment")
                    ,exactly(0)
            );
        }
        assertEquals("a no payment integration for Cash on delivery" ,0 , mappingRepo.countByOrganizationIdAndMappingType_TypeName(ORG_ID, PAYMENT.getValue()));
    }



    private void assertPaymentMappingCreated(PaymentEntity payment){
        IntegrationMappingEntity paymentMapping =
                mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(ORG_ID, PAYMENT.getValue(), payment.getId().toString())
                        .orElse(null);
        assertNotNull("a Payment is created for each sub-order", paymentMapping);

        assertEquals(
                "single payment in nasnav can be saved as multiple payments in external systems, on for each sub-order"
                , 2,  new JSONArray(paymentMapping.getRemoteValue()).length());
    }


    private Order createNewOrder(String token) throws BusinessException, IOException {
        String req =
                TestCommons.json()
                        .put("shipping_service_id", "TEST")
                        .put("customer_address", 12300001L)
                        .put("additional_data", TestCommons.json().put(SHOP_ID, 50001))
                        .toString();
        ResponseEntity<Order> response =
                template
                    .postForEntity(
                            "/cart/checkout"
                            , getHttpEntity(req, token)
                            , Order.class);
        return response.getBody();
    }



    private String getIntegrationModuleRequestJson() {
        try {
            return objectMapper.writeValueAsString(getMsDynamicsIntegrationInfo(serverFullUrl));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            assertTrue(false);
            return "";
        }
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


    private PaymentEntity createDummyPayment(Order order) {

        PaymentEntity payment = new PaymentEntity();
        JSONObject paymentObj =
                TestCommons.json()
                        .put("what_is_this?", "dummy_payment_obj");

        payment.setOperator("UPG");
        payment.setUid("MLB-<MerchantReference>");
        payment.setExecuted(new Date());
        payment.setObject(paymentObj.toString());
        payment.setAmount(order.getTotal());
        payment.setCurrency(EGP);
        payment.setStatus(PAID);
        payment.setUserId(order.getUserId());
        payment.setMetaOrderId(order.getOrderId());

        payment= paymentRepo.saveAndFlush(payment);
        return payment;
    }
}
