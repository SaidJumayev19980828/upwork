package com.nasnav.test.shipping.services.pickup_point;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

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
import com.nasnav.dto.request.shipping.ShippingAdditionalDataDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data_7.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class PickupPointServiceTest {
	
	@Autowired
    private TestRestTemplate template;
	
	@Autowired
	private ObjectMapper mapper;
	
	
	//create offers, always return stocks in warehouse
	@Test
	public void createOfferTest() throws Exception{
		Long customerAddress = 12300001L;
		HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<String> response = 
        		template.exchange("/shipping/offers?customer_address="+customerAddress, GET, request, String.class);

        String body = ofNullable(response.getBody()).orElse("[]");
        List<ShippingOfferDTO> offers = mapper.readValue(body, new TypeReference<List<ShippingOfferDTO>>(){});
        List<Long> shops = getOfferedShops(offers);
        
        assertEquals(OK, response.getStatusCode());
        assertEquals(1, offers.size());
        assertTrue(asList(503L).stream().allMatch(shops::contains));
	}
	
	
	
	
	
	
	private List<Long> getOfferedShops(List<ShippingOfferDTO> offers) {
		return offers
				.stream()
				.map(ShippingOfferDTO::getAdditionalData)
				.flatMap(List::stream)
				.map(ShippingAdditionalDataDTO::getOptions)
				.flatMap(List::stream)
				.map(option -> Long.valueOf(option))
				.collect(toList());
	}
}





