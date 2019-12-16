import static com.nasnav.enumerations.OrderFailedStatus.INVALID_ORDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
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
import com.nasnav.dao.OrdersRepository;
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
import com.nasnav.test.helpers.TestHelper;

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
	
	//TODO: test adding bundle product as basket item, test the quantity calculation will work
	
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
	UserService userService;

	@Mock
	private OrdersController ordersController;


	@Autowired
	private TestHelper helper;
	
	
	@Test
	public void unregisteredUser() {
		StocksEntity stock = createStock();
		
		//---------------------------------------------------------------
		
		JSONObject request = createOrderRequestWithBasketItems(OrderStatus.NEW, item(stock.getId(), stock.getQuantity()));
		ResponseEntity<String> response = template.postForEntity("/order/update"
															, TestCommons.getHttpEntity(request.toString(), "XX")
															, String.class);
		
		//---------------------------------------------------------------
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	

	@Test
	public void addNewOrderWithEmptyBasket() {
		JSONObject request = createOrderRequestWithBasketItems(OrderStatus.NEW);
		ResponseEntity<OrderResponse> response = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(request.toString(), "123")
										, OrderResponse.class);
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}

	
	
	
	
	@Test
	public void updateOrderSuccessTest() {
		StocksEntity stock = createStock();
		
		// create a new order, then take it's oder id and try to make an update using it
		JSONObject request = createOrderRequestWithBasketItems(OrderStatus.NEW, item(stock.getId(), stock.getQuantity()));
		ResponseEntity<OrderResponse> response = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(request.toString(), "123")
										, OrderResponse.class);

		// get the returned orderId
		long orderId = response.getBody().getOrderId();
		
		//---------------------------------------------------------------
		
		assertEquals(HttpStatus.OK, response.getStatusCode());

		//---------------------------------------------------------------
		// make an update request using the created order
		JSONObject updateRequest = createOrderRequestWithBasketItems(OrderStatus.CLIENT_CONFIRMED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<OrderResponse> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), "123")
										, OrderResponse.class);
		System.out.println("----------response-----------------" + updateResponse);
		

		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
	}
	
	
	
	

	@Test
	public void updateOrderNonExistingOrderIdTest() {
		// try updating with a non-existing order number
		JSONObject updateRequest = createOrderRequestWithBasketItems(OrderStatus.CLIENT_CONFIRMED);
		updateRequest.put("order_id", 9584875);
		
		ResponseEntity<OrderResponse> response = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity( updateRequest.toString(), "123")
										, OrderResponse.class);

		assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}
	
	
	
	

	@Test
	public void createOrderWithoutProvidingValidStockIdTest() {
		// try updating with a non-existing stock id
		JSONObject request = createOrderRequestWithBasketItems(OrderStatus.NEW, item(9845757332L, 4));
		ResponseEntity<String> response = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(request.toString(), "123")
										, String.class);
		
		//---------------------------------------------------------------
		JSONObject body = new JSONObject(response.getBody());
		assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		assertEquals(INVALID_ORDER.toString(), body.get("error"));
	}
	
	
	


	@Test
	public void createnewOrder() throws JsonParseException, Exception, Exception {
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
			//---------------------------------------------------------------
			JSONObject request = createOrderRequestWithBasketItems(OrderStatus.NEW, item(stocksEntity.getId(), quantity));
			ResponseEntity<String> response = 
					template.postForEntity("/order/update"
											, TestCommons.getHttpEntity( request.toString(), persistentUser.getAuthenticationToken())
											, String.class);
			
			//---------------------------------------------------------------
			if (scope != 2) {
				ObjectMapper mapper = new ObjectMapper();
				OrderResponse body = mapper.readValue(response.getBody(), OrderResponse.class);
				
				assertEquals(HttpStatus.OK, response.getStatusCode());
				assertEquals(itemPrice.multiply(new BigDecimal(quantity)), body.getPrice());
				assertNotNull(body.getOrderId());
			} else {
				JSONObject body = new JSONObject(response.getBody());
				// with quantity = 0, order shall not be placed
				assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
				assertEquals(INVALID_ORDER.toString(), body.get("error"));
			}
		}
		
	}

	
	
	

	@Test
	public void updateNewOrder() {
		UserEntity persistentUser = userRepository.getByEmailAndOrganizationId("user1@nasnav.com", 99001L);
		
		Long stockId = 601L;
		StocksEntity stocksEntity = stockRepository.findById(stockId).get();
		
		Integer quantity = 5;
		BigDecimal itemPrice = new BigDecimal(500).setScale(2);			
		modifyStockData(stocksEntity, quantity, itemPrice);

		//---------------------------------------------------------------
		
		OrdersEntity ordersEntity = createOrderInDB(stocksEntity);
		Long orderId = ordersEntity.getId(); 
		
		//---------------------------------------------------------------
		// try updating with a non-existing order number
		JSONObject request = createOrderRequestWithBasketItems(OrderStatus.NEW, item(stocksEntity.getId(), quantity));
		request.put("order_id", orderId);
		ResponseEntity<OrderResponse> response = 
					template.postForEntity("/order/update"
								, TestCommons.getHttpEntity(request.toString(), persistentUser.getAuthenticationToken())
								, OrderResponse.class);

		//---------------------------------------------------------------
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(orderId, response.getBody().getOrderId());
		
		OrdersEntity updatedOrder = helper.getOrderEntityFullData(orderId);
		Set<BasketsEntity> basket = updatedOrder.getBasketsEntity();
		assertEquals(1, basket.size());
		
		BasketsEntity item = basket.stream().findFirst().get();
		assertEquals(stocksEntity.getId(), item.getStocksEntity().getId());
		assertEquals(stocksEntity.getQuantity().intValue(), item.getQuantity().intValue());
	}




	private OrdersEntity createOrderInDB(StocksEntity stocksEntity) {
		BigDecimal amount = new BigDecimal(500.25);		
		ShopsEntity shopsEntity = stocksEntity.getShopsEntity();
		OrganizationEntity organizationEntity = stocksEntity.getOrganizationEntity();
		
		OrdersEntity ordersEntity = new OrdersEntity();		
		ordersEntity.setAmount(amount);
		ordersEntity.setShopsEntity(shopsEntity);
		ordersEntity.setStatus(OrderStatus.NEW.getValue());
		ordersEntity.setCreationDate( LocalDateTime.now()  );
		ordersEntity.setUpdateDate( LocalDateTime.now()  );
		ordersEntity.setOrganizationEntity(organizationEntity);
		ordersEntity = orderRepository.save(ordersEntity);
		return ordersEntity;
	}




	private void modifyStockData(StocksEntity stocksEntity, Integer quantity, BigDecimal itemPrice) {
		stocksEntity.setPrice(itemPrice);
		stocksEntity.setQuantity(quantity);
		stocksEntity = stockRepository.save(stocksEntity);
	}
	
	
	
	
	

	@Test
	public void updateOrderNonExistingStatusTest() throws Exception{
		UserEntity persistentUser = userRepository.getByEmailAndOrganizationId("user1@nasnav.com", 99001L);
		StocksEntity stock = createStock();
		
		//---------------------------------------------------------------
		
		// create a new order, then take it's order id and try to make an update using it
		JSONObject request = createOrderRequestWithBasketItems(OrderStatus.NEW, item(stock.getId(), stock.getQuantity()));
		ResponseEntity<String> response = template.postForEntity("/order/update"
														, TestCommons.getHttpEntity(request.toString(), persistentUser.getAuthenticationToken())
														, String.class);

		// get the returned orderId
		OrderResponse body = readOrderReponse(response);
		long orderId = body.getOrderId();
		
		//---------------------------------------------------------------
		
		// try updating with a non-existing status
		JSONObject updateRequest = createOrderRequestWithBasketItems(OrderStatus.NEW);
		updateRequest.put("status", "NON_EXISTING_STATUS");
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<String> updateResponse  = 
				template.postForEntity("/order/update"
							, TestCommons.getHttpEntity( updateRequest.toString(), persistentUser.getAuthenticationToken())
							, String.class);
		//---------------------------------------------------------------
		assertEquals(HttpStatus.NOT_ACCEPTABLE, updateResponse.getStatusCode());
	}




	private OrderResponse readOrderReponse(ResponseEntity<String> response)
			throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		OrderResponse body = mapper.readValue(response.getBody(), OrderResponse.class);
		return body;
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
		HttpHeaders header = TestCommons.getHeaders("101112");
		// no filters
		ResponseEntity<String> response = template.exchange("/order/list?details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);

		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("all orders ",16,count);

		// by org_id
		response = template.exchange("/order/list?org_id=99001&details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("7 orders with org_id = 99001",7,count);

		// by store_id
		response = template.exchange("/order/list?store_id=501&details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("4 orders with store_id = 501",4,count);

		// by user_id
		response = template.exchange("/order/list?user_id=88&details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("6 orders with user_id = 88",6,count);

		// by status
		response = template.exchange("/order/list?status=NEW&details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("8 orders with status = NEW",8,count);

		// by org_id and status
		response = template.exchange("/order/list?org_id=99001&status=NEW&details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("3 orders with org_id = 99001 and status = NEW",3,count);

		// by org_id and store_id
		response = template.exchange("/order/list?org_id=99001&store_id=503&details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		// by org_id and user_id
		response = template.exchange("/order/list?org_id=99002&user_id=90&details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("2 order with org_id = 99002 and user_id = 90",2,count);

		// by store_id and status
		response = template.exchange("/order/list?store_id=501&status=NEW&details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("2 orders with order_id = 501 and status = NEW",2,count);


		// by user_id and status
		response = template.exchange("/order/list?user_id=88&status=NEW&details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("4 orders with user_id = 88 and status = NEW",4,count);


		// by user_id, store_id and status
		response = template.exchange("/order/list?user_id=88&store_id=501&status=NEW&details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("1 order with user_id = 88 and store_id = 501 and status = NEW",1,count);
	}
	
	
	
	
	

	@Test // Organization roles diffterent filters test
	public void ordersListOrganizationDifferentFiltersTest() {
		HttpHeaders header = TestCommons.getHeaders("161718");
		ResponseEntity<String> response = template.exchange("/order/list?details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("user#70 is Organization employee in org#99003 so he can view all orderes within org#99003", 7, count);

		header = TestCommons.getHeaders("131415");
		response = template.exchange("/order/list?details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("user#69 is Organization admin in org#99002 so he can view all orderes within org#99002", 6, count);

		header = TestCommons.getHeaders("192021");
		response = template.exchange("/order/list?details_level=2", HttpMethod.GET, new HttpEntity<>(header), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("user#71 is store employee in store#802 so he can view all orderes within store#802", 1, count);
	}
	
	
	
	

	@Test
	public void ordersListUnAuthTest() {
		// invalid user-id test
		ResponseEntity<String> response = template.exchange("/order/list?store_id=501", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("NO_EXISATING_TOKEN")), String.class); //no user with id = 99

		assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
	}
	
	
	
	
	

	@Test
	public void ordersListInvalidfiltersTest() {
		// by store_id only
		ResponseEntity<String> response = template.exchange("/order/list?store_id=550&details_level=2", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);
		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();
		assertTrue(200 == response.getStatusCode().value());
		assertEquals("No orders with store_id = 550 ", 0, count);

		// by user_id
		response = template.exchange("/order/list?user_id=99&details_level=2", HttpMethod.GET, new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("no orders with user_id = 99",0,count);

		// by org_id
		response = template.exchange("/order/list?org_id=999999&details_level=2", HttpMethod.GET, new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("no orders with org_id = 999999",0,count);

		// by status
		response = template.exchange("/order/list?status=invalid_status&details_level=2", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);

		assertTrue(400 == response.getStatusCode().value());
	}
	
	
	
	

	@Test
	public void testOrdersConsistency(){
		List<OrdersEntity> ordersList = orderRepository.findAll();

		for(OrdersEntity order : ordersList) {
			assertTrue(order.getUserId() != null);
		}
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getOrderInfoTest() throws JsonParseException, JsonMappingException, IOException {
			
		ResponseEntity<String> response = template.exchange("/order/info?order_id=330002&details_level=2"
														, HttpMethod.GET
														,new HttpEntity<>(TestCommons.getHeaders("101112"))
														, String.class);
		
		System.out.println("Order >>>> " + response.getBody());
		
		DetailedOrderRepObject body = readDetailedOrderRepObjectResponse(response);
		
		DetailedOrderRepObject expected = createExpectedOrderInfo(330002L, new BigDecimal("600.00"), 14, "CLIENT_CONFIRMED", 88L);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(expected, body);
	}
	
	
	
	
	
	@Test
	public void addNewOrderWithNonExistingItemToEmptyBasket() {
		JSONObject request = createOrderUpdateRequestWithNonExistingStock();
		
		ResponseEntity<String> response = 
				template.postForEntity("/order/update"
									, TestCommons.getHttpEntity(request.toString(), "123")
									, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}
	
	
	
	
	
	@Test
	public void addNewOrderWithTooHighQuantityToEmptyBasket() {
		JSONObject request = createOrderUpdateRequestWithInvalidQuantity();
		
		ResponseEntity<String> response = 
				template.postForEntity("/order/update"
									, TestCommons.getHttpEntity(request.toString(), "123")
									, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	public void updateNewOrderWithTooHighQuantityToEmptyBasket() {
		JSONObject request = createOrderUpdateRequestWithInvalidQuantity();
		request.put("order_id", 33L);
		
		ResponseEntity<String> response = 
				template.postForEntity("/order/update"
									, TestCommons.getHttpEntity(request.toString(), "123")
									, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getCurrentOrderNoAuthTest() throws JsonParseException, JsonMappingException, IOException {
			
		ResponseEntity<String> response = template.getForEntity("/order/current", String.class);
				
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getCurrentOrderTest() throws JsonParseException, JsonMappingException, IOException {
			
		ResponseEntity<String> response = template.exchange("/order/current?details_level=2"
														, HttpMethod.GET
														, new HttpEntity<>(TestCommons.getHeaders("123"))
														, String.class);
		
		System.out.println("Order >>>> " + response.getBody());
		
		DetailedOrderRepObject body = readDetailedOrderRepObjectResponse(response);
		
		DetailedOrderRepObject expected = createExpectedOrderInfo(330003L, new BigDecimal("300.00"), 7, "NEW", 88L);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(expected, body);
	}






	private DetailedOrderRepObject readDetailedOrderRepObjectResponse(ResponseEntity<String> response)
			throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		DetailedOrderRepObject body = mapper.readValue(response.getBody(), DetailedOrderRepObject.class);
		return body;
	}
	
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getCurrentOrderNotFoundTest() throws JsonParseException, JsonMappingException, IOException {
			
		ResponseEntity<String> response = template.exchange("/order/current?details_level=2"
														, HttpMethod.GET
														, new HttpEntity<>(TestCommons.getHeaders("789"))
														, String.class);
		
		System.out.println("Order >>>> " + response.getBody());	
		
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
	
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getCurrentOrderUserHasNoOrdersTest() throws JsonParseException, JsonMappingException, IOException {
			
		ResponseEntity<String> response = template.exchange("/order/current?details_level=2"
														, HttpMethod.GET
														, new HttpEntity<>(TestCommons.getHeaders("011"))
														, String.class);
		
		System.out.println("Order >>>> " + response.getBody());	
		
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getCurrentOrderUserHasMultipleNewOrdersTest() throws JsonParseException, JsonMappingException, IOException {
			
		ResponseEntity<String> response = template.exchange("/order/current?details_level=2"
														, HttpMethod.GET
														, new HttpEntity<>(TestCommons.getHeaders("456"))
														, String.class);		
		
		System.out.println("Order >>>> " + response.getBody());
		
		DetailedOrderRepObject body = readDetailedOrderRepObjectResponse(response);
		
		DetailedOrderRepObject expected = createExpectedOrderInfo(330004L, new BigDecimal("200.00"), 5, "NEW", 89L);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(expected, body);
	}
	
	

	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void deleteCurrentOrderNoAuthTest() throws JsonParseException, JsonMappingException, IOException {
			
		ResponseEntity<String> response = template.exchange("/order/current"
															, HttpMethod.DELETE
															, new HttpEntity<>(TestCommons.getHeaders("NON_EXISTING_TOKEN"))
															, String.class);
				
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	
	

	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void deleteCurrentOrderTest() throws JsonParseException, JsonMappingException, IOException {
		long countAllBefore = orderRepository.count();
		long countBefore = orderRepository.countByStatusAndUserId(OrderStatus.NEW.getValue() , 89L);
		
		//-------------------------------------------
		
		ResponseEntity<String> response = template.exchange("/order/current"
															, HttpMethod.DELETE
															, new HttpEntity<>(TestCommons.getHeaders("456"))
															, String.class);
		
		//-------------------------------------------
		long countAfter = orderRepository.countByStatusAndUserId(OrderStatus.NEW.getValue() , 89L);
		long countAllAfter = orderRepository.count();
				
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotEquals( 0L, countBefore);
		assertNotEquals(countAllBefore, countBefore);
		assertEquals( 0L, countAfter);
		assertEquals("check that other users orders were not affected"
					, countBefore - countAfter
					, countAllBefore - countAllAfter);
	}
	
	
	
	
	
	
	
	private JSONObject createOrderUpdateRequestWithInvalidQuantity() {
		Integer qunantity = Integer.MAX_VALUE/100;		
		return createOrderRequestWithBasketItems(OrderStatus.NEW, item(601L, qunantity));
	}
	
	
	
	

	private JSONObject createOrderUpdateRequestWithNonExistingStock() {
		Long nonExistingId = Long.MAX_VALUE/100;		
		return createOrderRequestWithBasketItems(OrderStatus.NEW, item(nonExistingId, 333));
	}
	
	
	
	
	
	private JSONObject createOrderRequestWithBasketItems(OrderStatus status, Item... items) {
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
	
	

	private DetailedOrderRepObject createExpectedOrderInfo(Long orderId, BigDecimal price, Integer quantity, String status, Long userId) {
		OrdersEntity entity = orderRepository.findById(orderId).get();
		
		DetailedOrderRepObject order = new DetailedOrderRepObject();
		order.setUserId(userId);
		order.setCurrency("EGP");
		order.setCreatedAt( entity.getCreationDate() );
		order.setDeliveryDate( entity.getDeliveryDate() );
		order.setOrderId( orderId );
		order.setShipping( BigDecimal.ZERO );
		order.setShippingAddress( createExpectedShippingAddr() );
		order.setShopId( entity.getShopsEntity().getId() );
		order.setStatus( status );
		order.setSubtotal( price );
		order.setTotal( price);		
		order.setItems( createExpectedItems(price, quantity));
		order.setTotalQuantity(quantity);
		order.setPaymentStatus(entity.getPaymentStatus().toString());
		
		return order;
	}

	private ShippingAddress createExpectedShippingAddr() {
		ShippingAddress addr = new ShippingAddress();
		addr.setDetails("");
		return addr;
	}
	
	
	

	private List<BasketItem> createExpectedItems(BigDecimal price, Integer quantity) {
		BasketItem item = new BasketItem();
		item.setProductId(1001L);
		item.setName("product_1");
		item.setStockId( 601L );
		item.setQuantity(quantity);
		item.setTotalPrice( price );
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
