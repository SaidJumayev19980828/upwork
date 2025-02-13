package com.nasnav.test.shipping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static java.util.Optional.ofNullable;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)

@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ShippingTest extends AbstractTestWithTempBaseDir {
	
	
	@Autowired
    private TestRestTemplate template;
	
	
	@Test
	public void getShippingOffersNoAuthz() {
        HttpEntity<?> request =  getHttpEntity("NOT FOUND");
        ResponseEntity<String> response = 
        		template.exchange("/shipping/offers", GET, request, String.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void getShippingOffersNoAuthN() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response = 
        		template.exchange("/shipping/offers?customer_address=-1", GET, request, String.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void getShippingOffers() throws Exception {
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<String> response = 
        		template.exchange("/shipping/offers?customer_address=12300003", GET, request, String.class);
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JSR310Module());
        
        String body = ofNullable(response.getBody()).orElse("[]");
        List<ShippingOfferDTO> offers = mapper.readValue(body, new TypeReference<List<ShippingOfferDTO>>(){});
        
        assertEquals(OK, response.getStatusCode());
        assertEquals(1, offers.size());
        
        ShippingOfferDTO offer = offers.get(0);
        assertEquals(2, offer.getShipments().size());
        assertEquals(0 , offer.getTotal().compareTo(new BigDecimal("51.00")));
        offer.getShipments().forEach(shipment -> assertEquals(0 , shipment.getShippingFee().compareTo(new BigDecimal("25.50"))));
	}
	
	
	//TODO: test address id doesn't exists or
	//TODO: test address id doesn't belong to user.
}
