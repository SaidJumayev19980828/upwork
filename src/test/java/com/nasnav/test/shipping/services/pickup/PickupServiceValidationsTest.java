package com.nasnav.test.shipping.services.pickup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nasnav.service.CartService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.commons.utils.MapBuilder;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.dto.query.result.CartCheckoutData;
import com.nasnav.service.OrderService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.ShippingManagementService;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data_3.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class PickupServiceValidationsTest {
	
	@MockBean
	private SecurityService securityService;
	
	@Autowired
	private ShippingManagementService shippingMgr;
	
	@Autowired
	private OrderService orderService;

	@Autowired
	private CartService cartService;
	
	@Before
	public void initMocks() {
		BaseUserEntity user = new UserEntity();
		user.setId(88L);
		when(securityService.getCurrentUserOrganizationId()).thenReturn(99001L);
		when(securityService.getCurrentUser()).thenReturn(user);
	}
	
	
	
	@Test
	public void validateCartSuccessTest() {
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
		List<CartCheckoutData> cartCheckoutData = getCheckoutDataFromCurrentCart();
		shippingMgr.validateCartForShipping(cartCheckoutData, dto);
		assertTrue("validate shipment, normal case, all items in an allowed shop", true);
	}



	
	private List<CartCheckoutData> getCheckoutDataFromCurrentCart() {
		return orderService.createCheckoutData(cartService.getCart());
	}



	@Test(expected = RuntimeBusinessException.class)
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data_4.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void validateCartWithMultipleShopsTest() {
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
		List<CartCheckoutData> cartCheckoutData = getCheckoutDataFromCurrentCart();
		shippingMgr.validateCartForShipping(cartCheckoutData, dto);
		assertFalse("validate shipment, items at different shops", true);
	}
	
	
	
	
	
	@Test(expected = RuntimeBusinessException.class)
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data_5.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void validateCartWithInvalidShopTest() {
		Long customerAddress = 12300001L;
		Map<String,String> params = 
				MapBuilder
				.<String,String>map()
				.put("SHOP_ID", "504")
				.getMap();
		CartCheckoutDTO dto = new CartCheckoutDTO();
		dto.setAddressId(customerAddress);
		dto.setServiceId("PICKUP");
		dto.setAdditionalData(params);
		List<CartCheckoutData> cartCheckoutData = getCheckoutDataFromCurrentCart();
		shippingMgr.validateCartForShipping(cartCheckoutData, dto);
		assertFalse("validate shipment, shop not allowed", true);
	}
	
	
	
	@Test(expected = RuntimeBusinessException.class)
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data_5.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void validateNoShopProvidedTest() {
		Long customerAddress = 12300001L;
		Map<String,String> params = new HashMap<>();
		
		CartCheckoutDTO dto = new CartCheckoutDTO();
		dto.setAddressId(customerAddress);
		dto.setServiceId("PICKUP");
		dto.setAdditionalData(params);
		
		List<CartCheckoutData> cartCheckoutData = getCheckoutDataFromCurrentCart();
		shippingMgr.validateCartForShipping(cartCheckoutData, dto);
		assertFalse("validate shipment, no shop provided", true);
	}
}





