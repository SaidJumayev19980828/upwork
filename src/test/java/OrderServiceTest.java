import static com.nasnav.enumerations.OrderFailedStatus.INVALID_ORDER;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nasnav.NavBox;
import com.nasnav.controller.OrdersController;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.BasketItem;
import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.ShippingAddress;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.UserService;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class OrderServiceTest {
	private static final String EXPECTED_COVER_IMG_URL = "99001/cover_img.jpg";

	@Autowired
	private TestRestTemplate template;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrdersRepository orderRepository;
	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private BasketRepository basketRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	UserService userService;

	@Mock
	private OrdersController ordersController;



	@Test
	public void unregisteredUser() {
		StocksEntity stock = createStock();
		
		ResponseEntity response = template.postForEntity("/order/update",
				TestCommons.getHttpEntity("{ \"status\" : \"NEW\", \"basket\": [ { \"stock_id\":" + stock.getId() + ", \"quantity\": " +  stock.getQuantity() + "} ] }", 1, "XX"),
				Object.class);
		Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
		stockRepository.delete(stock);
	}

	// This needs fixing as it doesn't correctly use baskets
	// @Test
	public void createNewBasket() {
		ResponseEntity<OrderResponse> response = template.postForEntity("/order/update",
				TestCommons.getHttpEntity("{ \"basket\": [{ \"product\": 1234, \"quantity\": 4}] }",
						88, "123"),
				OrderResponse.class);
		Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
		Assert.assertTrue(response.getBody().isSuccess());
		// delete the order after assertion
		orderRepository.deleteById(response.getBody().getOrderId());
	}

	@Test
	public void addOrderNewStatusEmptyBasket() {
		ResponseEntity<OrderResponse> response = template.postForEntity("/order/update",
				TestCommons.getHttpEntity("{ \"status\": \"NEW\", \"basket\": [] }", 88,
						"123"),
				OrderResponse.class);
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertFalse(response.getBody().isSuccess());
	}

	// This needs fixing as it doesn't correctly use baskets
	@Test
	public void updateOrderSuccessTest() {
		StocksEntity stock = createStock();
		// create a new order, then take it's oder id and try to make an update using it
		ResponseEntity<OrderResponse> response = null;
		response = template.postForEntity("/order/update",
					TestCommons.getHttpEntity("{ \"status\" : \"NEW\", \"basket\": [ { \"stock_id\":" + stock.getId() + ", \"quantity\": " +  stock.getQuantity() + "} ] }",
							88, "123"),
					OrderResponse.class);

		// get the returned orderId
		long orderId = response.getBody().getOrderId();

		Assert.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
		Assert.assertTrue(response.getBody().isSuccess());

		try {
			// make a new request using the created order
			response = template.postForEntity("/order/update",
					TestCommons.getHttpEntity("{\"order_id\":" + orderId
									+ ", \"status\" : \"CLIENT_CONFIRMED\"}",
							88, "123"),
					OrderResponse.class);
			System.out.println("----------response-----------------" + response);
		}catch(Exception e){
			e.printStackTrace();
		}

		Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
		Assert.assertTrue(response.getBody().isSuccess());

		//delete baskets
		List<BasketsEntity> baskets = basketRepository.findByOrdersEntity_Id(orderId);
		for(BasketsEntity basket : baskets){
			basketRepository.delete(basket);
			stockRepository.delete(basket.getStocksEntity());
		}

		// delete the order after assertion
		orderRepository.deleteById(orderId);
	}

	@Test
	public void updateOrderNonExistingOrderIdTest() {
		// try updating with a non-existing order number
		ResponseEntity<OrderResponse> response = template.postForEntity("/order/update", TestCommons.getHttpEntity(
				"{\"id\": 250, \"status\" : \"CLIENT_CONFIRMED\", \"basket\": [{ \"product\": 1234, \"quantity\": 4}] }",
				88, "123"), OrderResponse.class);

		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertFalse(response.getBody().isSuccess());
	}

	@Test
	public void createOrderWithoutProvidingValidStockIdTest() {
		// try updating with a non-existing order number
		ResponseEntity<OrderResponse> response = template.postForEntity("/order/update",
				TestCommons.getHttpEntity(
						"{\"status\" : \"CLIENT_CONFIRMED\", \"basket\": [{ \"product\": 1234, \"quantity\": 4}] }",
						88, "123"),
				OrderResponse.class);

		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(INVALID_ORDER, response.getBody().getStatus());
		Assert.assertFalse(response.getBody().isSuccess());
	}


	@Test
	public void createnewOrder() {
		UserEntity persistentUser = userRepository.getByEmailAndOrganizationId("user1@nasnav.com", 99001L);
		Integer quantity = 5;
		BigDecimal itemPrice = new BigDecimal(500).setScale(2);

		// testing different combinations of price/quantity
		// scope == 1: both price and quantity available in the table
		for (int scope = 1; scope < 3; scope++) {			

			StocksEntity stocksEntity = stockRepository.findById(601L).get();
			if (scope < 3) {
				stocksEntity.setPrice(itemPrice);
			} else {
				stocksEntity.setPrice(new BigDecimal(0));
			}
			if (scope != 2) {
				stocksEntity.setQuantity(quantity);
			} else {
				stocksEntity.setQuantity(0);
			}
			stocksEntity = stockRepository.save(stocksEntity);

			ResponseEntity<OrderResponse> response = template.postForEntity("/order/update",
					TestCommons.getHttpEntity(
							"{ \"status\" : \"NEW\", \"basket\": [{ \"stock_id\": " + stocksEntity.getId()
									+ ", \"quantity\": " + quantity + "}] }",
							persistentUser.getId(), persistentUser.getAuthenticationToken()),
					OrderResponse.class);

			basketRepository.deleteByOrdersEntity_Id(response.getBody().getOrderId());

			if (response.getBody().getOrderId() != null) {
				orderRepository.deleteById(response.getBody().getOrderId());
			}

			if (scope != 2) {
				Assert.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
				Assert.assertNull(response.getBody().getStatus());
				Assert.assertTrue(response.getBody().isSuccess());
				Assert.assertEquals(itemPrice.multiply(new BigDecimal(quantity)), response.getBody().getPrice());
				Assert.assertNotNull(response.getBody().getOrderId());
			} else {
				// with quantity = 0, order shall not be placed
				Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
				Assert.assertEquals(INVALID_ORDER, response.getBody().getStatus());

			}
		}
		
	}


	@Test
	public void updateCurrentOrder() {
		UserEntity persistentUser = userRepository.getByEmailAndOrganizationId("user1@nasnav.com", 99001L);
		Long stockId = 601L;
		StocksEntity stocksEntity = stockRepository.findById(stockId).get();
		ShopsEntity shopsEntity = stocksEntity.getShopsEntity();

		
		//modify stock data for the test
		Integer quantity = 5;
		BigDecimal itemPrice = new BigDecimal(500).setScale(2);		
		stocksEntity.setPrice(itemPrice);
		stocksEntity.setQuantity(quantity);
		stocksEntity = stockRepository.save(stocksEntity);

		//create order
		BigDecimal amount = new BigDecimal(500.25);
		OrganizationEntity organizationEntity = stocksEntity.getOrganizationEntity();
		OrdersEntity ordersEntity = new OrdersEntity();		
		ordersEntity.setAmount(amount);
		ordersEntity.setShopsEntity(shopsEntity);
		ordersEntity.setStatus(OrderStatus.NEW.getValue());
		ordersEntity.setCreationDate( LocalDateTime.now()  );
		ordersEntity.setUpdateDate( LocalDateTime.now()  );
		ordersEntity.setOrganizationEntity(organizationEntity);
		ordersEntity = orderRepository.save(ordersEntity);

		// try updating with a non-existing order number
		ResponseEntity<OrderResponse> response = template.postForEntity("/order/update", TestCommons.getHttpEntity(
				"{ \"order_id\":\"" + ordersEntity.getId() + "\", \"status\" : \"NEW\", \"basket\": [{ \"stock_id\": "
						+ stockId + ", \"quantity\": " + quantity + "}] }",
				persistentUser.getId(), persistentUser.getAuthenticationToken()), OrderResponse.class);


		Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
		Assert.assertNull(response.getBody().getStatus());
		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertNotNull(response.getBody().getOrderId());
	}

	@Test
	public void updateOrderNonExistingStatusTest() {
		UserEntity persistentUser = userRepository.getByEmailAndOrganizationId("user1@nasnav.com", 99001L);
		StocksEntity stock = createStock();
		// create a new order, then take it's order id and try to make an update using it
		ResponseEntity<OrderResponse> response = null;
		response = template.postForEntity("/order/update",
				TestCommons.getHttpEntity("{ \"status\" : \"NEW\", \"basket\": [ { \"stock_id\":" + stock.getId() + ", \"quantity\": " +  stock.getQuantity() + "} ] }",
						persistentUser.getId(), persistentUser.getAuthenticationToken()),
				OrderResponse.class);

		// get the returned orderId
		long orderId = response.getBody().getOrderId();
		// try updating with a non-existing status
		response = template.postForEntity("/order/update", TestCommons.getHttpEntity(
				"{ \"order_id\": " + orderId + ", \"status\" : \"NON_EXISTING_STATUS\"}",
				persistentUser.getId(), persistentUser.getAuthenticationToken()), OrderResponse.class);

		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertFalse(response.getBody().isSuccess());

		//delete baskets
		List<BasketsEntity> baskets = basketRepository.findByOrdersEntity_Id(orderId);
		for(BasketsEntity basket : baskets){
			basketRepository.delete(basket);
			stockRepository.delete(basket.getStocksEntity());
		}

		orderRepository.deleteById(orderId);
	}
	
	
	

	private StocksEntity createStock() {		
		StocksEntity stock = stockRepository.findById(601L).get();
		stock.setPrice(new BigDecimal(100));
		stock.setQuantity(100);
		StocksEntity stockEntity = stockRepository.save(stock);
		return stockEntity;
	}
	
	
	

	@Test // Nasnav_Admin diffterent filters test
	public void ordersListNasnavAdminDifferentFiltersTest() {
		HttpHeaders header = TestCommons.getHeaders(68, "101112");
		// no filters
		ResponseEntity<String> response = template.exchange("/order/list", HttpMethod.GET, new HttpEntity<>(header), String.class);

		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("all orders ",16,count);

		// by org_id
		response = template.exchange("/order/list?org_id=99001", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("7 orders with org_id = 99001",7,count);

		// by store_id
		response = template.exchange("/order/list?store_id=501", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("4 orders with store_id = 501",4,count);

		// by user_id
		response = template.exchange("/order/list?user_id=88", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("6 orders with user_id = 88",6,count);

		// by status
		response = template.exchange("/order/list?status=NEW", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("8 orders with status = NEW",8,count);

		// by org_id and status
		response = template.exchange("/order/list?org_id=99001&status=NEW", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("3 orders with org_id = 99001 and status = NEW",3,count);

		// by org_id and store_id
		response = template.exchange("/order/list?org_id=99001&store_id=503", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		// by org_id and user_id
		response = template.exchange("/order/list?org_id=99002&user_id=90", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("2 order with org_id = 99002 and user_id = 90",2,count);

		// by store_id and status
		response = template.exchange("/order/list?store_id=501&status=NEW", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("2 orders with order_id = 501 and status = NEW",2,count);


		// by user_id and status
		response = template.exchange("/order/list?user_id=88&status=NEW", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("4 orders with user_id = 88 and status = NEW",4,count);


		// by user_id, store_id and status
		response = template.exchange("/order/list?user_id=88&store_id=501&status=NEW", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("1 order with user_id = 88 and store_id = 501 and status = NEW",1,count);
	}

	@Test // Organization roles diffterent filters test
	public void ordersListOrganizationDifferentFiltersTest() {
		HttpHeaders header = TestCommons.getHeaders(70, "161718");
		ResponseEntity<String> response = template.exchange("/order/list", HttpMethod.GET, new HttpEntity<>(header), String.class);
		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("user#70 is Organization employee in org#99003 so he can view all orderes within org#99003", 7, count);

		header = TestCommons.getHeaders(69, "131415");
		response = template.exchange("/order/list", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("user#69 is Organization admin in org#99002 so he can view all orderes within org#99002", 6, count);

		header = TestCommons.getHeaders(71, "192021");
		response = template.exchange("/order/list", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("user#71 is store employee in store#802 so he can view all orderes within store#802", 1, count);
	}

	@Test
	public void ordersListUnAuthTest() {
		// invalid user-id test
		ResponseEntity<String> response = template.exchange("/order/list?store_id=501", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(99, "789")), String.class); //no user with id = 99

		Assert.assertTrue(401 == response.getStatusCode().value());
		Assert.assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());

		// invalid user-token test
		response = template.exchange("/order/list?store_id=501", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(88, "invalidtoken")), String.class); //no user with token = invalidtoken

		Assert.assertTrue(401 == response.getStatusCode().value());
		Assert.assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
	}

	@Test
	public void ordersListInvalidfiltersTest() {
		// by store_id only
		ResponseEntity<String> response = template.exchange("/order/list?store_id=550", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(68, "101112")), String.class);
		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("No orders with store_id = 550 ", 0, count);

		// by user_id
		response = template.exchange("/order/list?user_id=99", HttpMethod.GET, new HttpEntity<>(TestCommons.getHeaders(68, "101112")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("no orders with user_id = 99",0,count);

		// by org_id
		response = template.exchange("/order/list?org_id=999999", HttpMethod.GET, new HttpEntity<>(TestCommons.getHeaders(68, "101112")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("no orders with org_id = 999999",0,count);

		// by status
		response = template.exchange("/order/list?status=invalid_status", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(68, "101112")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("get all orders if status parameter is invalid",16,count);
	}

	@Test
	public void testOrdersConsistency(){
		List<OrdersEntity> ordersList = orderRepository.findAll();

		for(OrdersEntity order : ordersList) {
			Assert.assertTrue(order.getUserId() != null);
		}
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getOrderInfoTest() throws JsonParseException, JsonMappingException, IOException {
			
		ResponseEntity<String> response = template.exchange("/order/info?order_id=33"
														, HttpMethod.GET
														,new HttpEntity<>(TestCommons.getHeaders(68, "101112"))
														, String.class);
		
		System.out.println("Order >>>> " + response.getBody());
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		DetailedOrderRepObject body = mapper.readValue(response.getBody(), DetailedOrderRepObject.class);
		
		DetailedOrderRepObject expected = createExpectedOrderInfo(33L);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(expected, body);
	}
	
	
	
	
	
	@Test
	public void addOrderNewStatusNonExistingItemToEmptyBasket() {
		JSONObject request = createOrderUpdateRequestWithNonExistingStock();
		
		ResponseEntity<OrderResponse> response = 
				template.postForEntity("/order/update"
									, TestCommons.getHttpEntity(request.toString() , 88, "123")
									, OrderResponse.class);
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}
	
	
	
	
	
	

	private JSONObject createOrderUpdateRequestWithNonExistingStock() {
		Long nonExistingId = Long.MAX_VALUE;		
		return createOrderUpdateRequestWithBasketItems(OrderStatus.NEW, item(nonExistingId, 333));
	}
	
	
	
	
	
	private JSONObject createOrderUpdateRequestWithBasketItems(OrderStatus status, Item... items) {
		JSONArray basket = createBasket( items);
		
		JSONObject request = new JSONObject();
		request.put("status", status.name());
		request.put("basket", basket);
		return request;
	}
	
	
	
	
	
	private Item item(Long stockId, Integer quantity) {
		return new Item(stockId, quantity);
	}
	
	
	
	
	private JSONArray createBasket(Item...items) {
		return new JSONArray( 
				Arrays.asList(items)
						.stream()
						.map(this::toJsonObject)				
						.collect(Collectors.toList())
				);
	}
	
	
	
	
	
	private JSONObject toJsonObject(Item item) {
		JSONObject basketItem = new JSONObject();		
		basketItem.put("stock_id", item.getStockId());
		basketItem.put("quantity", item.getQuantity());		
		return basketItem;
	}
	
	

	private DetailedOrderRepObject createExpectedOrderInfo(Long orderId) {
		OrdersEntity entity = orderRepository.findById(orderId).get();
		
		DetailedOrderRepObject order = new DetailedOrderRepObject();
		order.setUserId(88L);
		order.setCurrency("EGP");
		order.setCreatedAt( entity.getCreationDate() );
		order.setDeliveryDate( entity.getDeliveryDate() );
		order.setOrderId( orderId );
		order.setShipping( BigDecimal.ZERO );
		order.setShippingAddress( createExpectedShippingAddr() );
		order.setShopId( entity.getShopsEntity().getId() );
		order.setStatus("CLIENT_CONFIRMED");
		order.setSubtotal( new BigDecimal("600.00") );
		order.setTotal( new BigDecimal("600.00"));		
		order.setItems( createExpectedItems());
		order.setTotalQuantity(14);
		
		return order;
	}

	private ShippingAddress createExpectedShippingAddr() {
		ShippingAddress addr = new ShippingAddress();
		addr.setDetails("");
		return addr;
	}
	
	
	

	private List<BasketItem> createExpectedItems() {
		BasketItem item = new BasketItem();
		item.setProductId(1001L);
		item.setName("product_1");
		item.setStockId( 601L );
		item.setQuantity(14);
		item.setTotalPrice( new BigDecimal("600.00") );
		item.setThumb(EXPECTED_COVER_IMG_URL);
		return Arrays.asList(item);
	}
}


@Data
@AllArgsConstructor
class Item{
	private Long stockId;
	private Integer quantity;
}