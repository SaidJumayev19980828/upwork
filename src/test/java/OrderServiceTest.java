import static com.nasnav.enumerations.OrderFailedStatus.INVALID_ORDER;
import static com.nasnav.enumerations.OrderStatus.CLIENT_CONFIRMED;
import static com.nasnav.enumerations.OrderStatus.DELIVERED;
import static com.nasnav.enumerations.OrderStatus.NEW;
import static com.nasnav.enumerations.OrderStatus.STORE_CANCELLED;
import static com.nasnav.enumerations.OrderStatus.STORE_CONFIRMED;
import static com.nasnav.enumerations.PaymentStatus.PAID;
import static com.nasnav.enumerations.TransactionCurrency.EGP;
import static com.nasnav.test.commons.TestCommons.getHeaders;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.nasnav.dto.*;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.OrderService;
import com.nasnav.service.UserService;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.helpers.TestHelper;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
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
	private EmployeeUserRepository empRepository;

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
	
	
	@Autowired
	private BasketRepository basketRepository;
	
	
	@Autowired
	private JdbcTemplate jdbc;
	
	
	@Autowired
	private OrderService orderService;
	
	
	@Autowired
	private PaymentsRepository paymentRepository;
	
	@Test
	public void unregisteredUser() {
		StocksEntity stock = createStock();
		
		//---------------------------------------------------------------
		
		JSONObject request = createOrderRequestWithBasketItems(OrderStatus.NEW, item(stock.getId(), stock.getQuantity()));
		ResponseEntity<String> response = template.postForEntity("/order/create"
															, TestCommons.getHttpEntity(request.toString(), "XX")
															, String.class);
		
		//---------------------------------------------------------------
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	

	@Test
	public void addNewOrderWithEmptyBasket() {
		JSONObject request = createOrderRequestWithBasketItems(OrderStatus.NEW);
		ResponseEntity<OrderResponse> response = 
				template.postForEntity("/order/create"
										, TestCommons.getHttpEntity(request.toString(), "123")
										, OrderResponse.class);
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}

	
	
	
	
	@Test
	public void updateOrderSuccessTest() {
		// create a new order, then take it's order id and try to make an update using it
		Integer orderQuantity = 5;
		BigDecimal itemPrice = new BigDecimal(100).setScale(2);	
		StocksEntity stock = createStock(itemPrice, orderQuantity);		
				
		JSONObject request = createOrderRequestWithBasketItems(NEW, item(stock.getId(), stock.getQuantity()));
		ResponseEntity<OrderResponse> response = 
				template.postForEntity("/order/create"
										, TestCommons.getHttpEntity(request.toString(), "123")
										, OrderResponse.class);

		// get the returned orderId
		long orderId = response.getBody().getOrders().get(0).getId();
		
		//---------------------------------------------------------------
		
		assertEquals(HttpStatus.OK, response.getStatusCode());

		//---------------------------------------------------------------
		// make an update request using the created order
		JSONObject updateRequest = createOrderRequestWithBasketItems(CLIENT_CONFIRMED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<OrderResponse> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), "123")
										, OrderResponse.class);
		System.out.println("----------response-----------------" + updateResponse);
		
		//---------------------------------------------------------------
		OrderResponse body = updateResponse.getBody();
		assertEquals(OK, updateResponse.getStatusCode());
		
		assertEquals(itemPrice.multiply(new BigDecimal(orderQuantity)), body.getPrice());
		assertNotNull(body.getOrderId());
		
		OrdersEntity order = orderRepository.findById(body.getOrderId()).get();
		assertEquals("user1", order.getName());
		assertNotNull(order.getAddress());
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
				template.postForEntity("/order/create"
										, TestCommons.getHttpEntity(request.toString(), "123")
										, String.class);
		
		//---------------------------------------------------------------
		JSONObject body = new JSONObject(response.getBody());
		assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		assertEquals(INVALID_ORDER.toString(), body.get("error"));
	}
	
	
	


	@Test
	public void createNewOrder() throws JsonParseException, Exception, Exception {
		UserEntity persistentUser = userRepository.getByEmailAndOrganizationId("user1@nasnav.com", 99001L);
		
		Long stockId = 601L;
		Integer orderQuantity = 5;
		Integer stockQuantity = orderQuantity;				
		BigDecimal itemPrice = new BigDecimal(500).setScale(2);	
		
		StocksEntity stocksEntity = prepareStockForTest(stockId, stockQuantity, itemPrice);
		//---------------------------------------------------------------
		JSONObject request = createOrderRequestWithBasketItems(OrderStatus.NEW, item(stocksEntity.getId(), orderQuantity));
		ResponseEntity<String> response = 
				template.postForEntity("/order/create"
										, TestCommons.getHttpEntity( request.toString(), persistentUser.getAuthenticationToken())
										, String.class);
		
		System.out.println("--------response------\n" + response.getBody());
		//---------------------------------------------------------------
		ObjectMapper mapper = new ObjectMapper();
		OrderResponse body = mapper.readValue(response.getBody(), OrderResponse.class);
		
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(itemPrice.multiply(new BigDecimal(orderQuantity)), body.getPrice());
		Long orderId = body.getOrders().get(0).getId();
		assertNotNull(orderId);
		
		OrdersEntity order = orderRepository.findById(orderId).get();
		assertEquals("user1", order.getName());
		assertNotNull(order.getAddress());		
	}



	@Test
	public void createNewMultiStoreOrder()  {
		UserEntity persistentUser = userRepository.getByEmailAndOrganizationId("user1@nasnav.com", 99001L);

		prepareStockForTest(601L, 4, new BigDecimal(600));
		prepareStockForTest(602L, 4, new BigDecimal(1200));
		prepareStockForTest(603L, 4, new BigDecimal(200));
		prepareStockForTest(604L, 4, new BigDecimal(700));

		String requestBody = "{\"basket\": [{\"quantity\": 1,\"stock_id\": 601,\"unit\": \"kg\"}," +
										   "{\"quantity\": 1,\"stock_id\": 602,\"unit\": \"kg\"}," +
										   "{ \"quantity\": 1,\"stock_id\": 603,\"unit\": \"kg\"}," +
										   "{ \"quantity\": 1,\"stock_id\": 604,\"unit\": \"kg\"}]," +
										   "\"delivery_address\": \"Somewhere behind a grocery store\"}";
		ResponseEntity<OrderResponse> response = template.postForEntity("/order/create"
						, TestCommons.getHttpEntity(requestBody, persistentUser.getAuthenticationToken()), OrderResponse.class);

		System.out.println("--------response------\n" + response.getBody());
		//---------------------------------------------------------------
		/*ObjectMapper mapper = new ObjectMapper();
		OrderResponse body = mapper.readValue(response.getBody(), OrderResponse.class);*/
		List<OrderRepresentationObject> orders = response.getBody().getOrders();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new BigDecimal(2700).setScale(2), response.getBody().getPrice());
		assertEquals(2, orders.size());

		OrderRepresentationObject firstOrder = orders.get(0);
		OrderRepresentationObject secondOrder = orders.get(1);

		assertEquals(new BigDecimal(1800).setScale(2), firstOrder.getPrice());
		assertEquals(502, firstOrder.getShopId().intValue());
		assertOrderUserInfo(firstOrder.getId());

		assertEquals(new BigDecimal(900).setScale(2), secondOrder.getPrice());
		assertEquals(503, secondOrder.getShopId().intValue());
		assertOrderUserInfo(secondOrder.getId());

	}

	private void assertOrderUserInfo(Long orderId) {
		assertNotNull(orderId);
		OrdersEntity order = orderRepository.findById(orderId).get();
		assertEquals("user1", order.getName());
		assertNotNull(order.getAddress());
	}


	private StocksEntity prepareStockForTest(Long stockId, Integer stockQuantity, BigDecimal itemPrice) {
		StocksEntity stocksEntity = stockRepository.findById(stockId).get();
		stocksEntity.setPrice(itemPrice);
		stocksEntity.setQuantity(stockQuantity);
		stocksEntity = stockRepository.save(stocksEntity);
		return stocksEntity;
	}
	
	
	
	
	
	
	@Test
	public void createNewOrderWithZeroStock() throws JsonParseException, Exception, Exception {
		UserEntity persistentUser = userRepository.getByEmailAndOrganizationId("user1@nasnav.com", 99001L);
		
		Long stockId = 601L;
		Integer orderQuantity = 5;
		Integer stockQuantity = 0;				
		BigDecimal itemPrice = new BigDecimal(500).setScale(2);	
		
		prepareStockForTest(stockId, stockQuantity, itemPrice);		
		//---------------------------------------------------------------
		JSONObject request = createOrderRequestWithBasketItems(OrderStatus.NEW, item(stockId , orderQuantity));
		ResponseEntity<String> response = 
				template.postForEntity("/order/create"
										, TestCommons.getHttpEntity( request.toString(), persistentUser.getAuthenticationToken())
										, String.class);
		
		//---------------------------------------------------------------
		JSONObject body = new JSONObject(response.getBody());
		assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		assertEquals(INVALID_ORDER.toString(), body.get("error"));		
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
		
		OrdersEntity ordersEntity = createOrderInDB(stocksEntity, persistentUser.getId());
		Long orderId = ordersEntity.getId(); 
		
		//---------------------------------------------------------------
		// try updating with a existing order number
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




	private OrdersEntity createOrderInDB(StocksEntity stocksEntity, Long userId) {
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
		ordersEntity.setUserId(userId);
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
		ResponseEntity<String> response = template.postForEntity("/order/create"
														, TestCommons.getHttpEntity(request.toString(), persistentUser.getAuthenticationToken())
														, String.class);

		// get the returned orderId
		OrderResponse body = readOrderReponse(response);
		long orderId = body.getOrders().get(0).getId();
		
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
		return createStock(new BigDecimal("100"), 100);
	}
	
	
	
	
	private StocksEntity createStock(BigDecimal price, Integer Quantity) {		
		StocksEntity stock = stockRepository.findById(601L).get();
		stock.setPrice(price);
		stock.setQuantity(Quantity);
		StocksEntity stockEntity = stockRepository.save(stock);
		return stockEntity;
	}
	
	
	
	
	

	@Test // Nasnav_Admin diffterent filters test
	public void ordersListNasnavAdminDifferentFiltersTest() {
		HttpEntity<?> httpEntity = getHttpEntity("101112");
		// no filters
		ResponseEntity<String> response = template.exchange("/order/list?details_level=3"
															, HttpMethod.GET
															, httpEntity
															, String.class);

		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("all orders ",16,count);

		//---------------------------------------------------------------------
		// by org_id
		response = template.exchange("/order/list?org_id=99001&details_level=3"
										, HttpMethod.GET
										, httpEntity
										, String.class);		
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("7 orders with org_id = 99001",7,count);

		//---------------------------------------------------------------------
		// by shop_id
		response = template.exchange("/order/list?shop_id=501&details_level=3"
											, HttpMethod.GET
											, httpEntity
											, String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("4 orders with shop_id = 501",4,count);

		//---------------------------------------------------------------------
		// by user_id
		response = template.exchange("/order/list?user_id=88&details_level=3"
												, HttpMethod.GET
												, httpEntity
												, String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("6 orders with user_id = 88",6,count);

		//---------------------------------------------------------------------
		// by status
		response = template.exchange("/order/list?status=NEW&details_level=3"
											, HttpMethod.GET
											, httpEntity
											, String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("8 orders with status = NEW", 6,count);

		//---------------------------------------------------------------------
		// by org_id and status
		response = template.exchange("/order/list?org_id=99001&status=NEW&details_level=3"
											, HttpMethod.GET
											, httpEntity
											, String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("3 orders with org_id = 99001 and status = NEW",2 ,count);

		//---------------------------------------------------------------------
		// by org_id and shop_id
		response = template.exchange("/order/list?org_id=99001&shop_id=503&details_level=3"
										, HttpMethod.GET
										, httpEntity
										, String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		//---------------------------------------------------------------------
		// by org_id and user_id
		response = template.exchange("/order/list?org_id=99002&user_id=90&details_level=3"
										, HttpMethod.GET
										, httpEntity
										, String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("1 order with org_id = 99002 and user_id = 90", 1,count);

		//---------------------------------------------------------------------
		// by shop_id and status
		response = template.exchange("/order/list?shop_id=501&status=NEW&details_level=3"
										, HttpMethod.GET
										, httpEntity
										, String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("2 orders with shop_id = 501 and status = NEW",2,count);


		//---------------------------------------------------------------------
		// by user_id and status
		response = template.exchange("/order/list?user_id=88&status=NEW&details_level=3"
										, HttpMethod.GET
										, httpEntity
										, String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("2 orders with user_id = 88 and status = NEW", 2,count);


		//---------------------------------------------------------------------
		// by user_id, shop_id and status
		response = template.exchange("/order/list?user_id=88&shop_id=501&status=NEW&details_level=3"
										, HttpMethod.GET
										, httpEntity
										, String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("1 order with user_id = 88 and shop_id = 501 and status = NEW",1,count);
	}
	
	
	
	
	

	@Test // Organization roles diffterent filters test
	public void ordersListOrganizationDifferentFiltersTest() {
		ResponseEntity<String> response = template.exchange("/order/list?details_level=3"
																, HttpMethod.GET
																, getHttpEntity("161718")
																, String.class);
		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("user#70 is Organization employee in org#99003 so he can view all orderes within org#99003", 7, count);
		//-------------------------------------------------------------------------
		
		response = template.exchange("/order/list?details_level=3"
										, HttpMethod.GET
										, getHttpEntity("131415")
										, String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		long org99002Orders = orderRepository.countByOrganizationEntity_id(99002L);
		assertTrue(200 == response.getStatusCode().value());
		assertEquals("user#69 is Organization admin in org#99002 so he can view all orderes within org#99002", org99002Orders, count);

		//-------------------------------------------------------------------------
		response = template.exchange("/order/list?details_level=3"
										, HttpMethod.GET
										, getHttpEntity("192021")
										, String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		Long shopEmpId = 71L;
		Long shopId = empRepository.findById(shopEmpId)
									.map(EmployeeUserEntity::getShopId)
									.get();
		long shopOrdersCount = orderRepository.countByShopsEntity_id( shopId );
		assertTrue(200 == response.getStatusCode().value());
		assertEquals( 	format( "user#%d is store employee in store#%d so he can view all orderes within the store", shopEmpId, shopId)
						, shopOrdersCount
						, count);
	}
	
	
	
	

	@Test
	public void ordersListUnAuthTest() {
		// invalid user-id test
		ResponseEntity<String> response = template.exchange("/order/list?shop_id=501", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("NO_EXISATING_TOKEN")), String.class); //no user with id = 99

		assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
	}
	
	
	
	
	

	@Test
	public void ordersListInvalidfiltersTest() {
		// by shop_id only
		ResponseEntity<String> response = template.exchange("/order/list?shop_id=550", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);
		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();
		assertTrue(200 == response.getStatusCode().value());
		assertEquals("No orders with shop_id = 550 ", 0, count);

		// by user_id
		response = template.exchange("/order/list?user_id=99", HttpMethod.GET, new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("no orders with user_id = 99",0,count);

		// by org_id
		response = template.exchange("/order/list?org_id=999999", HttpMethod.GET, new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("no orders with org_id = 999999",0,count);

		// by status
		response = template.exchange("/order/list?status=invalid_status", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);

		assertTrue(400 == response.getStatusCode().value());
	}
	
	
	
	
	@Test
	public void testDateFilteration() {
		modifyOrderUpdateTime(330044L, LocalDateTime.of(2017, 11, 26, 10, 00, 00));
		modifyOrderUpdateTime(330045L, LocalDateTime.of(2017, 12, 15, 10, 00, 00));
		modifyOrderUpdateTime(330046L, LocalDateTime.of(2017, 12, 16, 10, 00, 00));
		
		//-------------------------------------------------------------------
		// by shop_id only
		ResponseEntity<String> response = 
				template.exchange(
						"/order/list?updated_before=2017-12-23:12:12:12"
								+ "&updated_after=2017-12-01:12:12:12"
						, HttpMethod.GET
						, getHttpEntity("101112")
						, String.class);
		
		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();
		
		assertTrue(200 == response.getStatusCode().value());
		assertEquals("expected 2 orders to be within this given time range ", 2, count);
	}






	private void modifyOrderUpdateTime(Long orderId, LocalDateTime newUpdateTime) {
		jdbc.update("update orders set updated_at = ? where id = ?", newUpdateTime, orderId);
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
			
		ResponseEntity<String> response = template.exchange("/order/info?order_id=330002&details_level=3"
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
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getOrderListLevelTwoTest() throws  IOException {

		ResponseEntity<String> response = template.exchange("/order/list?details_level=2&count=1", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);

		DetailedOrderRepObject body = getOrderListDetailedObject(response);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue( body.getTotalQuantity() != null);
		assertTrue( body.getTotalQuantity() == 3);
		assertEquals(null, body.getItems());
	}


	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getOrderListCountTest() throws  IOException {

		@SuppressWarnings("rawtypes")
		ResponseEntity<List> response = template.exchange("/order/list?count=1", HttpMethod.GET,
											new HttpEntity<>(TestCommons.getHeaders("101112")), List.class);

		assertEquals(1, response.getBody().size());

		response = template.exchange("/order/list?count=2", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("101112")), List.class);

		assertEquals(2, response.getBody().size());

		response = template.exchange("/order/list?count=4", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("101112")), List.class);

		assertEquals(4, response.getBody().size());
	}


	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getOrderListStartTest() throws  IOException { //count=1&

		ResponseEntity<String> response = template.exchange("/order/list?start=1&count=1&details_level=3", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);

		DetailedOrderRepObject body = getOrderListDetailedObject(response);
		DetailedOrderRepObject expectedBody = createExpectedOrderInfo(330005L, new BigDecimal("50"), 1, "NEW", 89L);

		assertEquals(expectedBody, body);


		response = template.exchange("/order/list?start=2&count=1&details_level=3", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);

		body = getOrderListDetailedObject(response);

		expectedBody = createExpectedOrderInfo(330003L, new BigDecimal("300"), 7, "NEW", 88L);

		assertEquals(expectedBody, body);


		response = template.exchange("/order/list?start=3&count=1&details_level=3", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders("101112")), String.class);

		body = getOrderListDetailedObject(response);

		expectedBody = createExpectedOrderInfo(330004L, new BigDecimal("200"), 5, "NEW", 89L);

		assertEquals(expectedBody, body);

	}

	private DetailedOrderRepObject getOrderListDetailedObject(ResponseEntity<String> response) throws IOException {
		JSONArray json = new JSONArray(response.getBody());

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());

		return mapper.readValue(json.getJSONObject(0).toString(), DetailedOrderRepObject.class);
	}
	
	
	@Test
	public void addNewOrderWithNonExistingItemToEmptyBasket() {
		JSONObject request = createOrderUpdateRequestWithNonExistingStock();
		
		ResponseEntity<String> response = 
				template.postForEntity("/order/create"
									, TestCommons.getHttpEntity(request.toString(), "123")
									, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}
	
	
	
	
	
	@Test
	public void addNewOrderWithTooHighQuantityToEmptyBasket() {
		JSONObject request = createOrderUpdateRequestWithInvalidQuantity();
		
		ResponseEntity<String> response = 
				template.postForEntity("/order/create"
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
			
		ResponseEntity<String> response = template.exchange("/order/current?details_level=3"
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
			
		ResponseEntity<String> response = template.exchange("/order/current?details_level=3"
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
			
		ResponseEntity<String> response = template.exchange("/order/current?details_level=3"
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
			
		ResponseEntity<String> response = template.exchange("/order/current?details_level=3"
														, GET
														, new HttpEntity<>(getHeaders("456"))
														, String.class);		
		
		System.out.println("Order >>>> " + response.getBody());
		
		DetailedOrderRepObject body = readDetailedOrderRepObjectResponse(response);
		
		DetailedOrderRepObject expected = createExpectedOrderInfo(330005L, new BigDecimal("50.00"), 1, "NEW", 89L);
		
		assertEquals(OK, response.getStatusCode());
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
		OrdersEntity entity = helper.getOrderEntityFullData(orderId);

		DetailedOrderRepObject order = new DetailedOrderRepObject();
		order.setUserId(userId);
		order.setUserName(entity.getName());
        order.setShopName(entity.getShopsEntity().getName());
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
	
	
	
	
	
	@Test
	public void userUpdateOrderForAnotherUser() {
		Long otherUserOrderId = 330033L;
		String userToken = "456"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderRequestWithBasketItems(CLIENT_CONFIRMED);
		updateRequest.put("order_id", otherUserOrderId);
		
		ResponseEntity<OrderResponse> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, OrderResponse.class);
		System.out.println("----------response-----------------\n" + updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.NOT_ACCEPTABLE, updateResponse.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void userUpdateBasketAfterConfrim() {
		Long orderId = 330033L;
		String userToken = "123"; 
		
		List<Long> basketItemsBefore = getBasketItemsIdList(orderId);
		//---------------------------------------------------------------
		
		JSONObject updateRequest = createOrderRequestWithBasketItems(CLIENT_CONFIRMED, item(602L, 14));
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<OrderResponse> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, OrderResponse.class);
		System.out.println("----------response-----------------" + updateResponse);
		
		//---------------------------------------------------------------
		List<Long> basketItemsAfter = getBasketItemsIdList(orderId);
		
		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
		assertEquals("any changes to the basket items are ignored if the order new status is not NEW"
						, basketItemsBefore
						, basketItemsAfter);
	}






	private List<Long> getBasketItemsIdList(Long orderId) {
		List<Long> basketItemsBefore = basketRepository.findByOrdersEntity_Id(orderId)
														.stream()
														.map(BasketsEntity::getId)
														.collect(Collectors.toList());
		return basketItemsBefore;
	}
	
	
	
	
	
	@Test
	public void storeManagerUpdateOrderForAnotherStore() {		
		Long orderId = 330036L;
		String userToken = "sdfe47"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderRequestWithBasketItems(STORE_CANCELLED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		System.out.println("----------response-----------------\n" + updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.FORBIDDEN, updateResponse.getStatusCode());
	}
	
	
	
	@Test
	public void  userUpdateOrderFromNewToDelievered() {
		Long otherUserOrderId = 330033L;
		String userToken = "123"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderRequestWithBasketItems(DELIVERED);
		updateRequest.put("order_id", otherUserOrderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		System.out.println("----------response-----------------\n" + updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.NOT_ACCEPTABLE, updateResponse.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void userUpadateOrderFromConfirmedToCancelled() {
		Long otherUserOrderId = 330040L;
		String userToken = "123"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderRequestWithBasketItems(OrderStatus.CLIENT_CANCELLED);
		updateRequest.put("order_id", otherUserOrderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		System.out.println("----------response-----------------\n" + updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
	}
	
	
	
	
	

	@Test
	public void userUpadateOrderFromStoreConfirmedToCancelled() {
		Long otherUserOrderId = 330042L;
		String userToken = "123"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderRequestWithBasketItems(OrderStatus.CLIENT_CANCELLED);
		updateRequest.put("order_id", otherUserOrderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		System.out.println("----------response-----------------\n" + updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.NOT_ACCEPTABLE, updateResponse.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void storeManagerUpdateOrderClientConfirmedToClientCancelled() {
		Long otherUserOrderId = 330046L;
		String userToken = "sdfe47"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderRequestWithBasketItems(OrderStatus.CLIENT_CANCELLED);
		updateRequest.put("order_id", otherUserOrderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		System.out.println("----------response-----------------\n" + updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.NOT_ACCEPTABLE, updateResponse.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void orgManagerUpadateOrderForAntherShop() {
		Long orderId = 330043L;
		String userToken = "131415"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderRequestWithBasketItems(STORE_CANCELLED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		System.out.println("----------response-----------------\n" + updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void orgManagerUpadateOrderForAntherOrg() {
		Long orderId = 330039L;
		String userToken = "131415"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderRequestWithBasketItems(STORE_CANCELLED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		System.out.println("----------response-----------------\n" + updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.FORBIDDEN, updateResponse.getStatusCode());
	}
	
	
	
	
	@Test
	public void userUpadateOrderAsConfirmedWithEmptyCart() {
		Long orderId = 330037L;
		String userToken = "123"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderRequestWithBasketItems(CLIENT_CONFIRMED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		System.out.println("----------response-----------------\n" + updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.NOT_ACCEPTABLE, updateResponse.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void managerCreatesNewOrder() {
		String userToken = "101112"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderRequestWithBasketItems(NEW);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/create"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		System.out.println("----------response-----------------\n" + updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.FORBIDDEN, updateResponse.getStatusCode());
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_2.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void managerConfirmsPaidOrder() {
		String userToken = "sdrf8s"; 
		Long orderId = 330033L;
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderRequestWithBasketItems(STORE_CONFIRMED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/update"
										, TestCommons.getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		System.out.println("----------response-----------------\n" + updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
		
		OrdersEntity updatedOrder = orderRepository.findById(orderId).get();
		assertEquals("payment status shouldn't change", PAID, updatedOrder.getPaymentStatus());
		assertEquals("payment status shouldn't change", STORE_CONFIRMED.getValue(), updatedOrder.getStatus());
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_2.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void userDeleteOrderWithSoftDeletedProducts() {
		long countAllBefore = orderRepository.count();
		long countBefore = orderRepository.countByStatusAndUserId(NEW.getValue() , 89L);
		
		//-------------------------------------------
		
		ResponseEntity<String> response = template.exchange("/order/current"
															, DELETE
															, new HttpEntity<>(getHeaders("456"))
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

	@Test
	public void testOrderListDeletion() {
		ResponseEntity<String> response = template.exchange("/order?order_ids=330035&order_ids=330037",
				DELETE,
				new HttpEntity<>(getHeaders("131415")),
				String.class);
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(14, orderRepository.findAll().size());
	}

	@Test
	public void testOrderListDeletionUnAuthorized() {
		ResponseEntity<String> response = template.exchange("/order?order_ids=330035&order_ids=330037",
				DELETE,
				new HttpEntity<>(TestCommons.getHeaders("sdrf8s")),
				String.class);
		assertEquals(403, response.getStatusCodeValue());
	}

	@Test
	public void testOrderListDeletionUnAuthenticated() {
		ResponseEntity<String> response = template.exchange("/order?order_ids=330035&order_ids=330037",
				DELETE,
				new HttpEntity<>(getHeaders("13141")),
				String.class);
		assertEquals(401, response.getStatusCodeValue());
	}

	@Test
	public void testOrderListDeletionOrderInDifferentOrg() {
		ResponseEntity<String> response = template.exchange("/order?order_ids=330033&order_ids=330037",
				DELETE,
				new HttpEntity<>(getHeaders("131415")),
				String.class);
		assertEquals(406, response.getStatusCodeValue());
	}


	@Test
	public void testOrderListDeletionNotNewOrder() {
		ResponseEntity<String> response = template.exchange("/order?order_ids=330038&order_ids=330037",
				DELETE,
				new HttpEntity<>(getHeaders("131415")),
				String.class);
		assertEquals(406, response.getStatusCodeValue());
	}
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void testOrderConfirm() {
		//create order
		String token = "123";
		
		Long orderId = 330033L; 
		OrdersEntity order = orderRepository.findById(orderId).get();
		LocalDateTime initialUpdateTime = order.getUpdateDate();
		StocksEntity stockBefore = stockRepository.findById(601L).get();
		assertEquals(15, stockBefore.getQuantity().intValue());
		
		createDummyPayment(order);
		ResponseEntity<String> response = confirmOrder(token, orderId);
		
		assertEquals(OK, response.getStatusCode());
		
		
		OrdersEntity saved = orderRepository.findById(orderId).get();
		assertEquals(CLIENT_CONFIRMED.getValue(), saved.getStatus());
		assertTrue(saved.getUpdateDate().isAfter(initialUpdateTime));
		
		StocksEntity stockAfter = stockRepository.findById(601L).get();
		assertEquals(1, stockAfter.getQuantity().intValue());
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void testOrderCheckoutService() throws BusinessException {
		Long orderId = 330033L; 
		OrdersEntity order = orderRepository.findById(orderId).get();
		LocalDateTime initialUpdateTime = order.getUpdateDate();
		StocksEntity stockBefore = stockRepository.findById(601L).get();
		assertEquals(15, stockBefore.getQuantity().intValue());
		
		//-------------------------------------------
		orderService.checkoutOrder(orderId);
		//-------------------------------------------
		
		OrdersEntity saved = orderRepository.findById(orderId).get();
		assertEquals(CLIENT_CONFIRMED.getValue(), saved.getStatus());
		assertTrue(saved.getUpdateDate().isAfter(initialUpdateTime));
		
		StocksEntity stockAfter = stockRepository.findById(601L).get();
		assertEquals(1, stockAfter.getQuantity().intValue());
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_4.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void testOrderConfirmForBundle() {
		//create order
		String token = "123";
		
		Long orderId = 330033L; 
		OrdersEntity order = orderRepository.findById(orderId).get();
		
		BundleOrderTestStocks before = getStocksCountBefore();		
		validateStockQuantityBefore(before);
		
		//-----------------------------------------------------
		createDummyPayment(order);
		ResponseEntity<String> response = confirmOrder(token, orderId);

		//-----------------------------------------------------
		assertEquals(OK, response.getStatusCode());		
		OrdersEntity saved = orderRepository.findById(orderId).get();
		assertEquals(CLIENT_CONFIRMED.getValue(), saved.getStatus());
		
		validateStocksQuantities(before);
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_4.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void testOrderCheckoutForBundle() throws BusinessException {
		Long orderId = 330033L; 
		
		BundleOrderTestStocks before = getStocksCountBefore();		
		validateStockQuantityBefore(before);
		
		//-------------------------------------------
		orderService.checkoutOrder(orderId);
		//-------------------------------------------
				
		OrdersEntity saved = orderRepository.findById(orderId).get();
		assertEquals(CLIENT_CONFIRMED.getValue(), saved.getStatus());
		
		validateStocksQuantities(before);
	}

	
	
	@Test(expected = Throwable.class)
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void testOrderCheckoutValidateNotEnoughStock() throws BusinessException {
		Long orderId = 330037L; 
		StocksEntity stockBefore = stockRepository.findById(602L).get();
		assertEquals(0, stockBefore.getQuantity().intValue());
		
		//-------------------------------------------
		orderService.validateOrderIdsForCheckOut(asList(orderId));
		//-------------------------------------------		
	}


	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/database_cleanup.sql","/sql/Orders_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void updateOrderDeliveryAddressStatusNew() {
		JSONArray basket = createBasket( new Item(601L, 1));

		String body = json().put("order_id",330033)
							.put("delivery_address", "new address")
							.put("basket", basket)
							.put("status", "NEW").toString();

		HttpEntity<?> request = getHttpEntity(body, "123");
		ResponseEntity<OrderResponse> res = template.postForEntity("/order/update", request, OrderResponse.class);
		assertEquals(200, res.getStatusCodeValue());
		OrdersEntity order = orderRepository.findById(330033).get();
		assertEquals("new address",order.getAddress());
	}


	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/database_cleanup.sql","/sql/Orders_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void updateOrderDeliveryAddressStatusNonNew() {
		BasketItemDTO itemDTO = new BasketItemDTO(601L, 1, "KG");
		List<BasketItemDTO> basket = new ArrayList<>();
		basket.add(itemDTO);

		String body = json().put("order_id",330038)
				.put("delivery_address", "new address")
				.put("basket",basket)
				.put("status", "CLIENT_CANCELLED").toString();

		HttpEntity<?> request = getHttpEntity(body, "789");
		ResponseEntity<OrderResponse> res = template.postForEntity("/order/update", request, OrderResponse.class);
		assertEquals(200, res.getStatusCodeValue());
		OrdersEntity order = orderRepository.findById(330033).get();
		assertEquals("address",order.getAddress());
	}




	private void validateStockQuantityBefore(BundleOrderTestStocks before) {
		assertNotEquals(0, before.bundleStocks.intValue());
		assertNotEquals(0, before.bundleItem1Stocks.intValue());
		assertNotEquals(0, before.bundleItem2Stocks.intValue());
		assertNotEquals(0, before.otherProductStocks.intValue());
	}






	private BundleOrderTestStocks getStocksCountBefore() {
		BundleOrderTestStocks before = new BundleOrderTestStocks();
		before.bundleStocks = stockRepository.findById(601L).get().getQuantity();
		before.bundleItem1Stocks = stockRepository.findById(602L).get().getQuantity();
		before.bundleItem2Stocks = stockRepository.findById(603L).get().getQuantity();
		before.otherProductStocks = stockRepository.findById(604L).get().getQuantity();
		return before;
	}






	private void validateStocksQuantities(BundleOrderTestStocks before) {
		BundleOrderTestStocks after = new BundleOrderTestStocks();
		after.bundleStocks = stockRepository.findById(601L).get().getQuantity();
		after.bundleItem1Stocks = stockRepository.findById(602L).get().getQuantity();
		after.bundleItem2Stocks = stockRepository.findById(603L).get().getQuantity();
		after.otherProductStocks = stockRepository.findById(604L).get().getQuantity();
		
		assertEquals(before.bundleStocks, after.bundleStocks);
		assertEquals(before.bundleItem1Stocks - 2, after.bundleItem1Stocks.intValue());
		assertEquals(before.bundleItem2Stocks - 2, after.bundleItem2Stocks.intValue());
		assertEquals(before.otherProductStocks - 3, after.otherProductStocks.intValue());
	}
	
	
	
	
	
	private PaymentEntity createDummyPayment(OrdersEntity order) {
		
		PaymentEntity payment = new PaymentEntity();
		JSONObject paymentObj = 
				json()
				.put("what_is_this?", "dummy_payment_obj");
		
		payment.setOperator("UPG");
		payment.setUid("MLB-<MerchantReference>");
		payment.setExecuted(new Date());
		payment.setObject(paymentObj.toString());
		payment.setAmount(order.getAmount());
		payment.setCurrency(EGP);
		payment.setStatus(PAID);
		payment.setUserId(order.getUserId());
		
		payment= paymentRepository.saveAndFlush(payment);
		order.setPaymentEntity(payment);
		orderRepository.saveAndFlush(order);
		return payment;
	}

	
	
	
	private ResponseEntity<String> confirmOrder(String token, Long orderId) {
		JSONObject updateRequest = createOrderRequestWithBasketItems(CLIENT_CONFIRMED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/update"
										, getHttpEntity( updateRequest.toString(), token)
										, String.class);
		return updateResponse;			
	}


}


@Data
@AllArgsConstructor
class Item{
	private Long stockId;
	private Integer quantity;
}




class BundleOrderTestStocks{
	Integer bundleStocks;
	Integer bundleItem1Stocks;
	Integer bundleItem2Stocks;
	Integer otherProductStocks;
}
