package com.nasnav.test.shipping;

import static com.nasnav.shipping.services.DummyShippingService.ID;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static com.nasnav.test.commons.TestCommons.jsonArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.nasnav.dao.OrganizationShippingServiceRepository;
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
				.put("Hot Line", "19777")
				.put("Shops", jsonArray().put("Wiakiki"));
	}
	
	
	
	
	@Test
	public void testRegisterToShippingService() {
		Long orgId = 99002L;
		
		assertFalse(orgShippingSrvRepo.getByServiceIdAndOrganization_Id(ID, orgId).isPresent());
		
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
        		orgShippingSrvRepo.getByServiceIdAndOrganization_Id(ID, orgId).get();
        
        assertEquals(OK, response.getStatusCode());
        assertEquals(params.toString(), shippingService.getServiceParameters());
        assertEquals(ID, shippingService.getServiceId());
	}
}
