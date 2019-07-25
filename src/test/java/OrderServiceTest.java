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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Request;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.nasnav.response.OrderResponse;
import com.nasnav.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class OrderServiceTest {

//    private static String _authToken = "TestAuthToken";
//    private Long _testUserId = null;
	private UserEntity persistentUser;

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

	@Value("classpath:sql/Orders_Test_Data_Delete.sql")
	private Resource ordersDataDelete;

	@Autowired
	private DataSource datasource;

	@PostConstruct
	public void setupLoginUser() {
		persistentUser = userRepository.getByEmailAndOrganizationId("unavailable@nasnav.com",(long)15);
		if (persistentUser == null) {
			persistentUser = new UserEntity();
			persistentUser.setName("John Smith");
			persistentUser.setEmail("unavailable@nasnav.com");
			persistentUser.setCreatedAt(LocalDateTime.now());
			persistentUser.setUpdatedAt(LocalDateTime.now());
		}
		persistentUser.setEncPassword("---");
		userRepository.save(persistentUser);
	}

	@PreDestroy
	public void removeLoginUser() {
		if (persistentUser != null) {
			userRepository.delete(persistentUser);
		}
	}

	@Before
	public void setup() {
		performInsertSqlDataScript();
	}

	@After
	public void cleanup() {
//        userService.deleteUser(_testUserId);
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
			ScriptUtils.executeSqlScript(con, ordersDataDelete);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void unregisteredUser() {
		StocksEntity stock = createStock();
		ResponseEntity<OrderResponse> response = template.postForEntity("/order/update",
				TestCommons.getHttpEntity("{ \"status\" : \"NEW\", \"basket\": [ { \"stock_id\":" + stock.getId() + ", \"quantity\": " +  stock.getQuantity() + "} ] }", 1, "XX"),
				OrderResponse.class);
		Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
		stockRepository.delete(stock);
	}

	// This needs fixing as it doesn't correctly use baskets
	// @Test
	public void createNewBasket() {

		ResponseEntity<OrderResponse> response = template.postForEntity("/order/update",
				TestCommons.getHttpEntity("{ \"basket\": [{ \"product\": 1234, \"quantity\": 4}] }",
						persistentUser.getId(), persistentUser.getAuthenticationToken()),
				OrderResponse.class);

		Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
		Assert.assertTrue(response.getBody().isSuccess());

		// delete the order after assertion
		orderRepository.deleteById(response.getBody().getOrderId());
	}

	@Test
	public void addOrderNewStatusEmptyBasket() {

		ResponseEntity<OrderResponse> response = template.postForEntity("/order/update",
				TestCommons.getHttpEntity("{ \"status\": \"NEW\", \"basket\": [] }", persistentUser.getId(),
						persistentUser.getAuthenticationToken()),
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
							persistentUser.getId(), persistentUser.getAuthenticationToken()),
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
							persistentUser.getId(), persistentUser.getAuthenticationToken()),
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
		orderRepository.deleteById(orderId);
	}

	@Test
	public void updateOrderNonExistingOrderIdTest() {
		// try updating with a non-existing order number
		ResponseEntity<OrderResponse> response = template.postForEntity("/order/update", TestCommons.getHttpEntity(
				"{\"id\": 250, \"status\" : \"CLIENT_CONFIRMED\", \"basket\": [{ \"product\": 1234, \"quantity\": 4}] }",
				persistentUser.getId(), persistentUser.getAuthenticationToken()), OrderResponse.class);

		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertFalse(response.getBody().isSuccess());
	}

	@Test
	public void createOrderWithoutProvidingValidStockIdTest() {
		// try updating with a non-existing order number
		ResponseEntity<OrderResponse> response = template.postForEntity("/order/update",
				TestCommons.getHttpEntity(
						"{\"status\" : \"CLIENT_CONFIRMED\", \"basket\": [{ \"product\": 1234, \"quantity\": 4}] }",
						persistentUser.getId(), persistentUser.getAuthenticationToken()),
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
		shopsEntity.setName("any");
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

		orderRepository.deleteById(orderId);
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
		shopsEntity.setName("any");
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

	@Test
	public void ordersListDifferentFiltersTest() {
		// by store_id only
		ResponseEntity<String> response = template.exchange("/order/list?store_id=501", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(88, "abdcefg")), String.class);
		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();
		System.out.println(body.toString() + " " + count);

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("8 orders with order_id = 501",8,count);


		// by store_id and status
		response = template.exchange("/order/list?store_id=501&status=NEW", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(88, "abdcefg")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();
		System.out.println(body.toString() + " " + count);

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("4 orders with order_id = 501 and status = NEW",4,count);


		// by user_id and status
		response = template.exchange("/order/list?user_id=88&status=NEW", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(88, "abdcefg")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();
		System.out.println(body.toString() + " " + count);

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("4 orders with user_id = 88 and status = NEW",4,count);


		// by user_id, store_id and status
		response = template.exchange("/order/list?user_id=88&store_id=501&status=NEW", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(88, "abdcefg")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();
		System.out.println(body.toString() + " " + count);

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("2 orders with user_id = 88 and store_id = 501 and status = NEW",2,count);
	}

	@Test
	public void ordersListUnAuthTest() {
		// invalid user-id test
		ResponseEntity<String> response = template.exchange("/order/list?store_id=501", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(90, "abdcefg")), String.class); //no user with id = 90

		Assert.assertTrue(401 == response.getStatusCode().value());
		Assert.assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());

		// invalid user-token test
		response = template.exchange("/order/list?store_id=501", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(88, "invalidtoken")), String.class); //no user with id = 90

		Assert.assertTrue(401 == response.getStatusCode().value());
		Assert.assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
	}

	@Test
	public void ordersListInvalidfiltersTest() {
		// by store_id only
		ResponseEntity<String> response = template.exchange("/order/list?store_id=503", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(88, "abdcefg")), String.class);
		JSONArray body = new JSONArray(response.getBody());
		long count = body.length();
		System.out.println(body.toString() + " " + count);

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("No orders with store_id = 503 ", 0, count);

		// by user_id
		response = template.exchange("/order/list?user_id=90", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(88, "abdcefg")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();
		System.out.println(body.toString() + " " + count);

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("no orders with user_id = 90",0,count);

		// by status
		response = template.exchange("/order/list?user_id=88&status=invalid_status", HttpMethod.GET,
				new HttpEntity<>(TestCommons.getHeaders(88, "abdcefg")), String.class);
		body = new JSONArray(response.getBody());
		count = body.length();
		System.out.println(body.toString() + " " + count);

		Assert.assertTrue(200 == response.getStatusCode().value());
		Assert.assertEquals("no orders with status = invalid_status",0,count);
	}
}