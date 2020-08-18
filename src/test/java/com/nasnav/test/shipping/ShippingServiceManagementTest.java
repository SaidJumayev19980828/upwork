package com.nasnav.test.shipping;

import static com.nasnav.enumerations.OrderStatus.DISPATCHED;
import static com.nasnav.enumerations.ShippingStatus.EN_ROUTE;
import static com.nasnav.shipping.services.DummyShippingService.ID;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static com.nasnav.test.commons.TestCommons.jsonArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

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
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationShippingServiceRepository;
import com.nasnav.dao.ShipmentRepository;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationShippingServiceEntity;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ShippingServiceManagementTest {
	
	
	@Autowired
    private TestRestTemplate template;
	
	@Autowired
	private OrganizationShippingServiceRepository orgShippingSrvRepo;
	
	@Autowired
	private ShipmentRepository shipmentRepo;
	
	@Autowired
	private OrdersRepository orderRepo;
	
	@Test
	public void testRegisterToShippingServiceNoAuthz() {
        HttpEntity<?> request =  getHttpEntity("NOT FOUND");
        ResponseEntity<String> response = 
        		template.exchange("/organization/shipping/service", POST, request, String.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void testRegisterToShippingServiceNoAuthN() {
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<String> response = 
        		template.exchange("/organization/shipping/service", POST, request, String.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	@Test
	public void testRegisterToShippingServiceMissingParams() {
		String requstBody = 
				json()
				.put("service_id", ID)
				.put("service_parameters", json().put("invalid", "yes"))
				.toString();
        HttpEntity<?> request = getHttpEntity(requstBody, "101112");
        ResponseEntity<String> response = 
        		template.exchange("/organization/shipping/service", POST, request, String.class);

        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testRegisterToShippingServiceInvalidParamsType() {
		JSONObject params = createDummyServiceParams();
		params.put("Hot Line", jsonArray());
		
		String requstBody = 
				json()
				.put("service_id", ID)
				.put("service_parameters", params)
				.toString();
        HttpEntity<?> request = getHttpEntity(requstBody, "101112");
        ResponseEntity<String> response = 
        		template.exchange("/organization/shipping/service", POST, request, String.class);

        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}




	private JSONObject createDummyServiceParams() {
		return json()
				.put("Hot Line", 19777)
				.put("Shops", jsonArray().put("Wiakiki"));
	}
	
	
	
	
	@Test
	public void testRegisterToShippingService() {
		Long orgId = 99002L;
		
		assertFalse(orgShippingSrvRepo.getByOrganization_IdAndServiceId(orgId, ID).isPresent());
		
		JSONObject params = createDummyServiceParams();
		String requstBody = 
				json()
				.put("service_id", ID)
				.put("service_parameters", params)
				.toString();
        HttpEntity<?> request = getHttpEntity(requstBody, "131415");
        ResponseEntity<String> response = 
        		template.exchange("/organization/shipping/service", POST, request, String.class);

        OrganizationShippingServiceEntity shippingService = 
        		orgShippingSrvRepo.getByOrganization_IdAndServiceId(orgId, ID).get();
        
        assertEquals(OK, response.getStatusCode());
        assertEquals(params.toString(), shippingService.getServiceParameters());
        assertEquals(ID, shippingService.getServiceId());
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data_2.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void changeShippingStatusTest() {
		Long subOrderId = 330031L;
		OrdersEntity subOrderBefore = orderRepo.findFullDataById(subOrderId).get();
		Long shipmentId = subOrderBefore.getShipment().getId();
		
		assertNotEquals(DISPATCHED.getValue(), subOrderBefore.getStatus());
		//--------------------------------------------------------------
		JSONObject body = json().put("status", EN_ROUTE.getValue())
								.put("id", "330031")
								.put("message", "");
		HttpEntity<?> req = getHttpEntity(body.toString(), "none");
		ResponseEntity<String> res =  template.postForEntity("/callbacks/shipping/service/TEST/99001", req, String.class);
		
		//--------------------------------------------------------------
		assertEquals(200, res.getStatusCodeValue());
		assertEquals(10, shipmentRepo.findById(shipmentId).get().getStatus().intValue());
		
		OrdersEntity subOrderAfter = orderRepo.findFullDataById(subOrderId).get();
		assertEquals(DISPATCHED.getValue(), subOrderAfter.getStatus());
	}
}
