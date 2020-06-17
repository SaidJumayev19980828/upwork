package com.nasnav.test;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static java.util.Optional.ofNullable;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.math.BigDecimal;
import java.util.List;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ShippingTest {
	
	
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
        		template.exchange("/shipping/offers", GET, request, String.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void getShippingOffers() throws Exception {
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<String> response = 
        		template.exchange("/shipping/offers?customer_address=1001", GET, request, String.class);
        
        ObjectMapper mapper = new ObjectMapper();
        String body = ofNullable(response.getBody()).orElse("[]");
        List<ShippingOfferDTO> offers = mapper.readValue(body, new TypeReference<List<ShippingOfferDTO>>(){});
        
        assertEquals(OK, response.getStatusCode());
        assertEquals(1, offers.size());
        
        ShippingOfferDTO offer = offers.get(0);
        assertEquals(2, offer.getShipments());
        assertEquals(new BigDecimal("50"), offer.getTotal());
        assertEquals(new BigDecimal("25"), offer.getShipments().get(0).getShippingFee());
	}
	
	
	//TODO: test address id doesn't exists or
	//TODO: test address id doesn't belong to user.
}
