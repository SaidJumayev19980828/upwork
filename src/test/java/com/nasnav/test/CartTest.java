package com.nasnav.test;

import static com.nasnav.enumerations.OrderStatus.CLIENT_CANCELLED;
import static com.nasnav.enumerations.OrderStatus.CLIENT_CONFIRMED;
import static com.nasnav.enumerations.OrderStatus.STORE_CONFIRMED;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.SERVICE_ID;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dto.response.OrderConfrimResponseDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.dto.response.navbox.Shipment;
import com.nasnav.dto.response.navbox.SubOrder;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.ShipmentEntity;
import com.nasnav.service.OrderService;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class CartTest {

	@Autowired
    private TestRestTemplate template;

	@Autowired
	private CartItemRepository cartItemRepo;

	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private OrdersRepository orderRepo;
	
	@Autowired
	private MetaOrderRepository metaOrderRepo;
	
	
	@Test
	public void getCartNoAuthz() {
        HttpEntity<?> request =  getHttpEntity("NOT FOUND");
        ResponseEntity<Cart> response =
        		template.exchange("/cart", GET, request, Cart.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void getCartNoAuthN() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<Cart> response = 
        		template.exchange("/cart", GET, request, Cart.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test 
	public void getCartSuccess() {
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Cart> response = 
        		template.exchange("/cart", GET, request, Cart.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().getItems().size());
        assertProductNamesReturned(response);
	}





	private void assertProductNamesReturned(ResponseEntity<Cart> response) {
		List<String> expectedProductNames = asList("product_1", "product_4");
        boolean isProductNamesReturned = 
        		response
        		.getBody()
        		.getItems()
        		.stream()
        		.map(CartItem::getName)
        		.allMatch(name -> expectedProductNames.contains(name));
        assertTrue(isProductNamesReturned);
	}


	@Test
	public void addCartItemZeroQuantity() {
		Long itemsCountBefore = cartItemRepo.countByUser_Id(88L);

		JSONObject item = createCartItem();
		item.put("stock_id", 602);
		item.put("quantity", 0);

		HttpEntity<?> request =  getHttpEntity(item.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(200, response.getStatusCodeValue());
		assertEquals(itemsCountBefore - 1 , response.getBody().getItems().size());
	}


	@Test
	public void addCartItemSuccess() {
		addCartItems(88L, 606L, 1);
	}





	private void addCartItems(Long userId, Long stockId, Integer quantity) {
		Long itemsCountBefore = cartItemRepo.countByUser_Id(userId);

		JSONObject item = createCartItem(stockId, quantity);

		HttpEntity<?> request =  getHttpEntity(item.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(200, response.getStatusCodeValue());
		assertEquals(itemsCountBefore + 1 , response.getBody().getItems().size());
	}


	
	
	@Test
	public void addCartItemNoStock() {
		JSONObject item = createCartItem();
		item.remove("stock_id");

		HttpEntity<?> request =  getHttpEntity(item.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}


	
	@Test
	public void addCartItemNoQuantity() {
		JSONObject item = createCartItem();
		item.remove("quantity");

		HttpEntity<?> request =  getHttpEntity(item.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}

	
	
	

	@Test
	public void addCartItemNegativeQuantity() {
		JSONObject item = createCartItem();
		item.put("quantity", -1);

		HttpEntity<?> request =  getHttpEntity(item.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}


	
	@Test
	public void addCartNoAuthz() {
		JSONObject item = createCartItem();
		HttpEntity<?> request =  getHttpEntity(item.toString(), "NOT FOUND");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}

	
	

	@Test
	public void addCartNoAuthN() {
		JSONObject item = createCartItem();
		HttpEntity<?> request =  getHttpEntity(item.toString(), "101112");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(FORBIDDEN, response.getStatusCode());
	}


	@Test
	public void deleteCartItemSuccess() {
		Long itemsCountBefore = cartItemRepo.countByUser_Id(88L);
		Long itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
		HttpEntity<?> request =  getHttpEntity("123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item?item_id=" + itemId, DELETE, request, Cart.class);

		assertEquals(OK, response.getStatusCode());
		assertEquals(itemsCountBefore - 1 , response.getBody().getItems().size());
	}



	@Test
	public void removeCartNoAuthz() {
		Long itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
		HttpEntity<?> request =  getHttpEntity("NOT FOUND");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item?item_id=" + itemId, DELETE, request, Cart.class);

		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}


	@Test
	public void removeCartNoAuthN() {
		Long itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
		HttpEntity<?> request =  getHttpEntity("101112");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item?item_id=" + itemId, DELETE, request, Cart.class);

		assertEquals(FORBIDDEN, response.getStatusCode());
	}


	private JSONObject createCartItem(Long stockId, Integer qunatity) {
		JSONObject item = new JSONObject();
		item.put("stock_id", stockId);
		item.put("cover_img", "img");
		item.put("quantity", qunatity);

		return item;
	}
	
	
	private JSONObject createCartItem() {
		return createCartItem(606L, 1);
	}


	@Test
	public void checkoutCartSuccess() {
		checkoutCart();
	}





	private Order checkoutCart() {
		JSONObject requestBody = createCartCheckoutBody();

		Order body = checkOutCart(requestBody, new BigDecimal("3151"), new BigDecimal("3100") ,new BigDecimal("51"));
		
		return body;
	}





	private Order checkOutCart(JSONObject requestBody, BigDecimal total, BigDecimal subTotal, BigDecimal shippingFee) {
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", request, Order.class);
		assertEquals(200, res.getStatusCodeValue());
		
		Order order =  res.getBody();
		BigDecimal subOrderSubtTotalSum = getSubOrderSubTotalSum(order);
		BigDecimal subOrderTotalSum = getSubOrderTotalSum(order);
		BigDecimal subOrderShippingSum = getSubOrderShippingSum(order);
		
		assertTrue(order.getOrderId() != null);
		assertEquals(0 ,shippingFee.compareTo(order.getShipping()));
		assertEquals(0 ,subTotal.compareTo(order.getSubtotal()));
		assertEquals(0 ,total.compareTo(order.getTotal()));
		assertEquals(0 ,order.getShipping().compareTo(subOrderShippingSum));
		assertEquals(0 ,order.getSubtotal().compareTo(subOrderSubtTotalSum));
		assertEquals(0 ,order.getTotal().compareTo(subOrderTotalSum));
		return order;
	}





	private BigDecimal getSubOrderShippingSum(Order order) {
		return order
				.getSubOrders()
				.stream()
				.map(SubOrder::getShipment)
				.map(Shipment::getShippingFee)
				.reduce(ZERO, BigDecimal::add);
	}





	private BigDecimal getSubOrderTotalSum(Order order) {
		return order
		.getSubOrders()
		.stream()
		.map(SubOrder::getTotal)
		.reduce(ZERO, BigDecimal::add);
	}





	private BigDecimal getSubOrderSubTotalSum(Order order) {
		return order
		.getSubOrders()
		.stream()
		.map(SubOrder::getSubtotal)
		.reduce(ZERO, BigDecimal::add);
	}


	@Test
	public void checkoutCartNoAuthZ() {
		HttpEntity<?> request =  getHttpEntity("not_found");
		ResponseEntity<String> response = template.postForEntity("/cart/checkout", request, String.class);

		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}


	@Test
	public void checkoutCartNoAuthN() {
		HttpEntity<?> request =  getHttpEntity("101112");
		ResponseEntity<String> response = template.postForEntity("/cart/checkout", request, String.class);

		assertEquals(FORBIDDEN, response.getStatusCode());
	}


	@Test
	public void checkoutCartNoAddressId() {
		JSONObject body = createCartCheckoutBody();
		body.put("customer_address", -1);

		HttpEntity<?> request = getHttpEntity(body.toString(), "123");
		ResponseEntity<String> response = template.postForEntity("/cart/checkout", request, String.class);
		assertEquals(406, response.getStatusCodeValue());
	}


	@Test
	public void checkoutCartInvalidAddressId() {
		JSONObject body = new JSONObject();
		body.put("shipping_service_id", "Bosta");

		HttpEntity<?> request = getHttpEntity(body.toString(), "123");
		ResponseEntity<String> response = template.postForEntity("/cart/checkout", request, String.class);
		assertEquals(406, response.getStatusCodeValue());
	}


	@Test
	public void checkoutCartDifferentCurrencies() {
		//first add item with different currency
		JSONObject item = createCartItem();
		item.put("stock_id", 606);
		HttpEntity<?> request = getHttpEntity(item.toString(), "123");
		ResponseEntity<Cart> response = template.exchange("/cart/item", POST, request, Cart.class);
		assertEquals(200, response.getStatusCodeValue());

		//then try to checkout cart
		JSONObject body = createCartCheckoutBody();

		request = getHttpEntity(body.toString(), "123");
		ResponseEntity<String> res = template.postForEntity("/cart/checkout", request, String.class);
		assertEquals(406, res.getStatusCodeValue());
	}


	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_2.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartZeroStock() {
		JSONObject body = createCartCheckoutBody();

		HttpEntity<?> request = getHttpEntity(body.toString(), "123");
		ResponseEntity<String> res = template.postForEntity("/cart/checkout", request, String.class);
		assertEquals(406, res.getStatusCodeValue());
	}


	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_3.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartDifferentOrg() {
		JSONObject body = createCartCheckoutBody();
		cartItemRepo.deleteByQuantityAndUser_Id(0, 88L);

		HttpEntity<?> request = getHttpEntity(body.toString(), "123");
		ResponseEntity<String> res = template.postForEntity("/cart/checkout", request, String.class);
		assertEquals(406, res.getStatusCodeValue());
		assertTrue(res.getBody().contains("O$CRT$0005"));
	}


	private JSONObject createCartCheckoutBody() {
		JSONObject body = new JSONObject();
		Map<String, String> additionalData = new HashMap<>();
		additionalData.put("name", "Shop");
		additionalData.put("value", "14");
		body.put("customer_address", 12300001);
		body.put("shipping_service_id", "TEST");
		body.put("additional_data", additionalData);

		return body;
	}
	
	
	
	// TODO: make this test work with a swtich flag, that either make it work on bosta
	//staging server + mail.nasnav.org mail server
	//or make it work on mock bosta server + mock mail service
//	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_4.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void orderCompleteCycle() throws BusinessException {

		addCartItems(88L, 602L, 2);
		addCartItems(88L, 604L, 1);
		
		//checkout
		JSONObject requestBody = createCartCheckoutBodyForCompleteCycleTest();

		Order order = checkOutCart(requestBody, new BigDecimal("3125"), new BigDecimal("3100"), new BigDecimal("25"));
		Long orderId = order.getOrderId();
		
		orderService.finalizeOrder(orderId);
		
		asList(new ShopManager(502L, "161718"), new ShopManager(501L,"131415"))
		.forEach(mgr -> confrimOrder(order, mgr));
	}
	
	
	
	
	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_5.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartAndDeleteDanglingOrdersTest() {
		Long unpaidOrderId = 310001L;
		Long cancelPaymentOrderId = 310002L;
		Long paidOrderId = 310003L;
		Long errorPaymentOrderId = 310004L;
		
		assertOrdersStatusBeforeCheckout(unpaidOrderId, cancelPaymentOrderId, paidOrderId, errorPaymentOrderId);
		
		JSONObject requestBody = createCartCheckoutBody();
		checkOutCart(requestBody, new BigDecimal("3151"), new BigDecimal("3100") ,new BigDecimal("51"));
		
		assertOrdersStatusAfterCheckout(unpaidOrderId, cancelPaymentOrderId, paidOrderId, errorPaymentOrderId);
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_6.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartSameCityTest() {
		
		//---------------------------------------------------------------		
		String requestBody = 
				json()
				.put("strategy", "SAME_CITY")
				.put("parameters", 
						json()
						.put("CUSTOMER_ADDRESS_ID",12300001L ))
				.toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res = 
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);
		
		//---------------------------------------------------------------
		Cart cart = res.getBody().getCart();
		List<Long> stockIdsAfter = 
				cart.getItems().stream().map(CartItem::getStockId).collect(toList());
		
		assertEquals(OK, res.getStatusCode());
		assertTrue(res.getBody().getTotalChanged());		
		assertEquals(2, cart.getItems().size());
		assertTrue("The optimization should pick stocks from a shop in cairo that can provide most items"
					, asList(607L, 609L).stream().allMatch(stockIdsAfter::contains));
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_7.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartSelectShopWithHighestStockTest() {
		
		//---------------------------------------------------------------		
		String requestBody = 
				json()
				.put("strategy", "SAME_CITY")
				.put("parameters", 
						json()
						.put("CUSTOMER_ADDRESS_ID",12300001L ))
				.toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res = 
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);
		
		//---------------------------------------------------------------
		Cart cart = res.getBody().getCart();
		List<Long> stockIdsAfter = 
				cart.getItems().stream().map(CartItem::getStockId).collect(toList());
		
		assertEquals(OK, res.getStatusCode());
		assertFalse("prices doesn't change", res.getBody().getTotalChanged());		
		assertEquals(3, cart.getItems().size());
		assertTrue("The optimization should pick 2 stocks from a shop in cairo that has the largest average stock quantity, "
				+ "and the third remaining stock from another shop in cairo with less stock quantity"
					, asList(607L, 608L, 612L).stream().allMatch(stockIdsAfter::contains));
	}
	
	
	
	

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_7.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartSelectShop() {
		
		//---------------------------------------------------------------		
		String requestBody = 
				json()
				.put("strategy", "SAME_CITY")
				.put("parameters", 
						json()
						.put("CUSTOMER_ADDRESS_ID",12300001L )
						.put("SHOP_ID",503L ))
				.toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res = 
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);
		
		//---------------------------------------------------------------
		Cart cart = res.getBody().getCart();
		List<Long> stockIdsAfter = 
				cart.getItems().stream().map(CartItem::getStockId).collect(toList());
		
		assertEquals(OK, res.getStatusCode());
		assertFalse("prices doesn't change", res.getBody().getTotalChanged());		
		assertEquals(3, cart.getItems().size());
		assertTrue("The optimization should pick the stocks from a the given shop even if it is in another city."
					, asList(601L, 602L, 603L).stream().allMatch(stockIdsAfter::contains));
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_7.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartSelectShopThatHaveNoEnoughQuantity() {
		
		//---------------------------------------------------------------		
		String requestBody = 
				json()
				.put("strategy", "SAME_CITY")
				.put("parameters", 
						json()
						.put("CUSTOMER_ADDRESS_ID",12300001L )
						.put("SHOP_ID",504L ))
				.toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res = 
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);
		
		//---------------------------------------------------------------
		Cart cart = res.getBody().getCart();
		List<Long> stockIdsAfter = 
				cart.getItems().stream().map(CartItem::getStockId).collect(toList());
		
		assertEquals(OK, res.getStatusCode());
		assertFalse("prices doesn't change", res.getBody().getTotalChanged());		
		assertEquals(3, cart.getItems().size());
		assertTrue("The optimization should pick the stocks from a the given shop , but the shop doesn't "
				+ "have enougj quantity for first item, so , it will pickit from the shop next in priority "
				+ " which should be in the same city and with highest average stock."
					, asList(607L, 611L, 612L).stream().allMatch(stockIdsAfter::contains));
	}
	
	
	
	
	
	
	@Test
	public void optimizeCartNoAuthz() {
        HttpEntity<?> request =  getHttpEntity("NOT FOUND");
        ResponseEntity<Cart> response =
        		template.exchange("/cart/optimize", POST, request, Cart.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void optimizeCartNoAuthN() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<Cart> response = 
        		template.exchange("/cart/optimize", POST, request, Cart.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	//TODO: invalid startegy test
	//TODO: invalid parameter json test
	





	private void assertOrdersStatusAfterCheckout(Long unpaidOrderId, Long cancelPaymentOrderId, Long paidOrderId, Long errorPaymentOrderId) {
		MetaOrderEntity unpaidOrderAfter = metaOrderRepo.findFullDataById(unpaidOrderId).get();
		MetaOrderEntity cancelPaymentOrderAfter = metaOrderRepo.findFullDataById(cancelPaymentOrderId).get();
		MetaOrderEntity paidOrderAfter = metaOrderRepo.findFullDataById(paidOrderId).get();
		MetaOrderEntity errorPaymentOrder = metaOrderRepo.findFullDataById(errorPaymentOrderId).get();
		
		assertEquals(CLIENT_CANCELLED.getValue(), unpaidOrderAfter.getStatus());
		assertEquals(CLIENT_CANCELLED.getValue(), cancelPaymentOrderAfter.getStatus());
		assertEquals(CLIENT_CANCELLED.getValue(), errorPaymentOrder.getStatus());
		assertEquals(STORE_CONFIRMED.getValue(), paidOrderAfter.getStatus());
		
		asList(unpaidOrderAfter, cancelPaymentOrderAfter, errorPaymentOrder)
		.stream()
		.map(MetaOrderEntity::getSubOrders)
		.flatMap(Set::stream)
		.forEach(subOrder -> assertEquals(CLIENT_CANCELLED.getValue(), subOrder.getStatus()));
		
		paidOrderAfter
		.getSubOrders()
		.stream()
		.forEach(subOrder -> assertEquals(STORE_CONFIRMED.getValue(), subOrder.getStatus()));
	}





	private void assertOrdersStatusBeforeCheckout(Long unpaidOrderId, Long cancelPaymentOrderId, Long paidOrderId, Long errorPaymentOrderId) {
		MetaOrderEntity unpaidOrder = metaOrderRepo.findFullDataById(unpaidOrderId).get();
		MetaOrderEntity cancelPaymentOrder = metaOrderRepo.findFullDataById(cancelPaymentOrderId).get();
		MetaOrderEntity paidOrder = metaOrderRepo.findFullDataById(paidOrderId).get();
		MetaOrderEntity errorPaymentOrder = metaOrderRepo.findFullDataById(errorPaymentOrderId).get();
		
		assertEquals(CLIENT_CONFIRMED.getValue(), unpaidOrder.getStatus());
		assertEquals(CLIENT_CONFIRMED.getValue(), cancelPaymentOrder.getStatus());
		assertEquals(CLIENT_CONFIRMED.getValue(), errorPaymentOrder.getStatus());
		assertEquals(STORE_CONFIRMED.getValue(), paidOrder.getStatus());
		
		asList(unpaidOrder, cancelPaymentOrder, errorPaymentOrder)
		.stream()
		.map(MetaOrderEntity::getSubOrders)
		.flatMap(Set::stream)
		.forEach(subOrder -> assertEquals(CLIENT_CONFIRMED.getValue(), subOrder.getStatus()));
		
		paidOrder
		.getSubOrders()
		.stream()
		.forEach(subOrder -> assertEquals(STORE_CONFIRMED.getValue(), subOrder.getStatus()));
	}
	



	private void confrimOrder(Order order, ShopManager mgr) {
		Long subOrderId = getSubOrderIdOfShop(order, mgr.getShopId());
		HttpEntity<?> request = getHttpEntity(mgr.getManagerAuthToken());
		ResponseEntity<OrderConfrimResponseDTO> res = 
				template.postForEntity("/order/confirm?order_id=" + subOrderId, request, OrderConfrimResponseDTO.class);
		
		String billFile = res.getBody().getShippingBill();
		OrdersEntity orderEntity = orderRepo.findByIdAndShopsEntity_Id(subOrderId, mgr.getShopId()).get();
		ShipmentEntity shipment = orderEntity.getShipment();
		
		assertFalse(billFile.isEmpty());
		assertNotNull(shipment.getTrackNumber());
		assertNotNull(shipment.getExternalId());
		assertEquals(STORE_CONFIRMED.getValue(), orderEntity.getStatus());
	}


	private Long getSubOrderIdOfShop(Order order, Long shopId) {
		return order
				.getSubOrders()
				.stream()
				.filter(ordr -> ordr.getShopId().equals(shopId))
				.map(SubOrder::getSubOrderId)
				.findFirst()
				.get();
	}
	
	
	
	
	private JSONObject createCartCheckoutBodyForCompleteCycleTest() {
		JSONObject body = new JSONObject();
		Map<String, String> additionalData = new HashMap<>();
		body.put("customer_address", 12300001);
		body.put("shipping_service_id", SERVICE_ID);
		body.put("additional_data", additionalData);

		return body;
	}
	
	
}



@Data
@AllArgsConstructor
class ShopManager{
	private Long shopId;
	private String managerAuthToken;
}
