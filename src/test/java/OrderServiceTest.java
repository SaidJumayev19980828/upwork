import static com.nasnav.enumerations.OrderFailedStatus.INVALID_ORDER;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import com.nasnav.persistence.*;
import org.json.JSONArray;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.nasnav.NavBox;
import com.nasnav.controller.OrdersController;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.UserService;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
public class OrderServiceTest {

	private static UserEntity persistentUser;

	private MockMvc mockMvc;

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
	private OrganizationRepository organizationRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ShopsRepository shopsRepository;

	@Autowired
	UserService userService;

	@Mock
	private OrdersController ordersController;

	@Value("classpath:sql/Orders_Test_Data_Insert.sql")
	private Resource ordersDataInsert;


	@Value("classpath:sql/database_cleanup.sql")
	private Resource databaseCleanup;

	@Autowired
	private DataSource datasource;

	@Before
	public void setup() {
		performDeleteSqlDataScript();
		performInsertSqlDataScript();
		persistentUser = userRepository.getByEmailAndOrganizationId("user1@nasnav.com", 99001L);
		if (persistentUser == null) {
			persistentUser = new UserEntity();
			persistentUser.setName("user1");
			persistentUser.setEmail("user1@nasnav.com");
			persistentUser.setCreatedAt(LocalDateTime.now());
			persistentUser.setUpdatedAt(LocalDateTime.now());
			persistentUser.setAuthenticationToken("7657595");
			persistentUser.setOrganizationId(99001L);
			persistentUser.setEncryptedPassword("---");
			userRepository.save(persistentUser);
		}
	}

	@After
	public  void cleanup() {
		performDeleteSqlDataScript();
	}

	public void performInsertSqlDataScript() {
		try (Connection con = datasource.getConnection()) {
			ScriptUtils.executeSqlScript(con, ordersDataInsert);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void performDeleteSqlDataScript() {
		try (Connection con = datasource.getConnection()) {
			ScriptUtils.executeSqlScript(con, databaseCleanup);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void unregisteredUser() {
		StocksEntity stock = createStock();
		
		ResponseEntity response = template.postForEntity("/order/update",
				TestCommons.getHttpEntity("{ \"status\" : \"NEW\", \"basket\": [ { \"stock_id\":" + stock.getId() + ", \"quantity\": " +  stock.getQuantity() + "} ] }", 1, "XX"),
				Object.class);
		Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());

		Long shopId = stock.getShopsEntity().getId();
		stockRepository.delete(stock);
		shopsRepository.deleteById(shopId);
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
			productRepository.delete(basket.getStocksEntity().getProductEntity());
		}

		// delete the order after assertion
		Long shopId = (orderRepository.findById(orderId).get()).getShopsEntity().getId();
		orderRepository.deleteById(orderId);
		shopsRepository.deleteById(shopId);
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
		Integer quantity = 5;
		BigDecimal itemPrice = new BigDecimal(500).setScale(2);

		// scope == 1: both price and quantity available in the table
		for (int scope = 1; scope < 3; scope++) {
			ShopsEntity shopsEntity = new ShopsEntity();
			shopsEntity.setName("any");
			shopsEntity.setCreatedAt(new Date());
			shopsEntity.setUpdatedAt(new Date());
			shopsEntity = shopsRepository.save(shopsEntity);

			StocksEntity stocksEntity = new StocksEntity();
			stocksEntity.setCreationDate(new Date());
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
			stocksEntity.setUpdateDate(new Date());
			stocksEntity.setShopsEntity(shopsEntity);
			stocksEntity = stockRepository.save(stocksEntity);

			ResponseEntity<OrderResponse> response = template.postForEntity("/order/update",
					TestCommons.getHttpEntity(
							"{ \"status\" : \"NEW\", \"basket\": [{ \"stock_id\": " + stocksEntity.getId()
									+ ", \"quantity\": " + quantity + "}] }",
							persistentUser.getId(), persistentUser.getAuthenticationToken()),
					OrderResponse.class);

			basketRepository.deleteByOrdersEntity_Id(response.getBody().getOrderId());
			stockRepository.delete(stocksEntity);

			if (response.getBody().getOrderId() != null) {
				orderRepository.deleteById(response.getBody().getOrderId());
			}
			shopsRepository.delete(shopsEntity);

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

		BigDecimal amount = new BigDecimal(500.25);
		ShopsEntity shopsEntity = new ShopsEntity();
		shopsEntity.setName("any name");
		shopsEntity.setCreatedAt(new Date());
		shopsEntity.setUpdatedAt(new Date());
		shopsEntity = shopsRepository.save(shopsEntity);
		Integer quantity = 5;
		BigDecimal itemPrice = new BigDecimal(500).setScale(2);
		Long stockId = null;
		StocksEntity stocksEntity = new StocksEntity();
		stocksEntity.setCreationDate(new Date());
		stocksEntity.setPrice(itemPrice);
		stocksEntity.setQuantity(quantity);
		stocksEntity.setUpdateDate(new Date());
		stocksEntity.setShopsEntity(shopsEntity);
		stocksEntity = stockRepository.save(stocksEntity);

		stockId = stocksEntity.getId();

		OrdersEntity ordersEntity = new OrdersEntity();
//		ordersEntity.setCreationDate(new Date());

		ordersEntity.setAmount(amount);
		ordersEntity.setShopsEntity(shopsEntity);
		ordersEntity.setStatus(OrderStatus.NEW.getValue());
		ordersEntity.setUpdateDate(new Date());
		ordersEntity = orderRepository.save(ordersEntity);

		// try updating with a non-existing order number
		ResponseEntity<OrderResponse> response = template.postForEntity("/order/update", TestCommons.getHttpEntity(
				"{ \"order_id\":\"" + ordersEntity.getId() + "\", \"status\" : \"NEW\", \"basket\": [{ \"stock_id\": "
						+ stockId + ", \"quantity\": " + quantity + "}] }",
				persistentUser.getId(), persistentUser.getAuthenticationToken()), OrderResponse.class);

		basketRepository.deleteByOrdersEntity_Id(response.getBody().getOrderId());
		stockRepository.delete(stocksEntity);
		orderRepository.delete(ordersEntity);
		shopsRepository.delete(shopsEntity);

		Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
		Assert.assertNull(response.getBody().getStatus());
		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertNotNull(response.getBody().getOrderId());
	}

	@Test
	public void updateOrderNonExistingStatusTest() {
		StocksEntity stock = createStock();
		// create a new order, then take it's oder id and try to make an update using it
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
			productRepository.delete(basket.getStocksEntity().getProductEntity());
		}

		Long shopId = (orderRepository.findById(orderId).get()).getShopsEntity().getId();
		orderRepository.deleteById(orderId);
		shopsRepository.deleteById(shopId);
	}

	private StocksEntity createStock() {
		//create product
		ProductEntity product = new ProductEntity();
		product.setName("product one");
		product.setCreationDate(new Date());
		product.setUpdateDate(new Date());
		ProductEntity productEntity = productRepository.save(product);

		BigDecimal amount = new BigDecimal(500.25);
		ShopsEntity shopsEntity = new ShopsEntity();
		shopsEntity.setName("any shop name");
		shopsEntity.setCreatedAt(new Date());
		shopsEntity.setUpdatedAt(new Date());
		shopsEntity = shopsRepository.save(shopsEntity);
		Integer quantity = 5;
		BigDecimal itemPrice = new BigDecimal(500).setScale(2);
		Long stockId = null;
		//create stock
		StocksEntity stock = new StocksEntity();
		stock.setPrice(new BigDecimal(100));
		stock.setCreationDate(new Date());
		stock.setUpdateDate(new Date());
		stock.setProductEntity(productEntity);
		stock.setQuantity(100);
		stock.setShopsEntity(shopsEntity);
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
}