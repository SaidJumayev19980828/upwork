package com.nasnav.test.shipping.services.pickup;

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
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.commons.utils.MapBuilder;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.shipping.ShippingAdditionalDataDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.service.SecurityService;
import com.nasnav.service.SecurityServiceImpl;
import com.nasnav.service.ShippingManagementService;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data_3.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class PickupServiceTest {
	
	@Autowired
	private ShippingManagementService shippingMgr;
	
	@Autowired
    private TestRestTemplate template;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private GenericWebApplicationContext context;
	
	//create offers, get shops that can provide whole cart
	//	- 2 shops that have enough stocks for the whole cart
	//	- 1 shop that have stock for only one item
	//	- 1 shop has all items but not enough stock
	//  - 1 shop is not in the allowed shops
	// expect the first two shops to be returned
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
        assertTrue(asList(503L,501L).stream().allMatch(shops::contains));
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
	
	
	//validate shipment, normal case, all items in an allowed shop
	@Test
	@DirtiesContext
	public void validateCartSuccessTest() {
		SecurityService mocked = Mockito.mock(SecurityServiceImpl.class);
		Mockito.when(mocked.getCurrentUserOrganizationId()).thenReturn(99001L);
		
		RootBeanDefinition beanDefinition = new RootBeanDefinition(SecurityService.class);
		beanDefinition.setInstanceSupplier(() -> mocked);
		context.removeBeanDefinition("securityServiceImpl");
		context.registerBeanDefinition("securityServiceImpl", beanDefinition);
		
		Long customerAddress = 12300001L;
		Map<String,String> params = 
				MapBuilder
				.<String,String>map()
				.put("SHOP_ID", "501")
				.getMap();
		CartCheckoutDTO dto = new CartCheckoutDTO();
		dto.setAddressId(customerAddress);
		dto.setServiceId("PICKUP");
		dto.setAdditionalData(params);
		shippingMgr.validateShippingAdditionalData(dto);
	}



	
	//validate shipment, items at different shops
	//validate shipment, shop not allowed
}





