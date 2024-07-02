package com.nasnav.test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.controller.OrdersController;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.response.OrderConfirmResponseDTO;
import com.nasnav.dto.response.OrderUserResponse;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.dto.response.navbox.SubOrder;
import com.nasnav.enumerations.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.OrdersListResponse;
import com.nasnav.service.MailService;
import com.nasnav.service.OrderService;
import com.nasnav.service.UserService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import com.nasnav.test.helpers.TestHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.constatnts.EmailConstants.ORDER_REJECT_TEMPLATE;
import static com.nasnav.enumerations.OrderStatus.*;
import static com.nasnav.enumerations.ShippingStatus.DRAFT;
import static com.nasnav.enumerations.ShippingStatus.REQUESTED;
import static com.nasnav.service.OrderService.BILL_EMAIL_SUBJECT;
import static com.nasnav.service.OrderService.ORDER_REJECT_SUBJECT;
import static com.nasnav.test.commons.TestCommons.*;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@Slf4j
public class OrderServiceTest extends AbstractTestWithTempBaseDir {
	
	//TODO: test adding bundle product as basket item, test the quantity calculation will work
	
	private static final String EXPECTED_COVER_IMG_URL = "99001/img1.jpg";
	private final String ORDER_LIST_API_PATH = "/order/list";

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
	private CartItemRepository cartRepo;
	@Autowired
	UserService userService;

	@Mock
	private OrdersController ordersController;

	@Autowired
	private TestHelper helper;

	@Autowired
	
	private JdbcTemplate jdbc;
	
	@Autowired
	private OrderService orderService;

	
	@MockBean
	private MailService mailService;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private MetaOrderRepository metaOrderRepo;
	
	@Autowired
	private PromotionsCodesUsedRepository usePromoRepo;

	@Autowired
	private ReferralWalletRepository referralWalletRepository;

	@Test
	public void updateOrderNonExistingOrderIdTest() {
		// try updating with a non-existing order number
		JSONObject updateRequest = createOrderStatusUpdateRequest(OrderStatus.CLIENT_CONFIRMED);
		updateRequest.put("order_id", 9584875);
		
		ResponseEntity<String> response =
				template.postForEntity("/order/status/update"
										, getHttpEntity( updateRequest.toString(), "131415")
										, String.class);

		assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}
	
	private StocksEntity createStock(BigDecimal price, Integer Quantity) {		
		StocksEntity stock = stockRepository.findById(601L).get();
		stock.setPrice(price);
		stock.setQuantity(Quantity);
		StocksEntity stockEntity = stockRepository.save(stock);
		return stockEntity;
	}
	
	
	
	
	

	@Test // MEETUSVR_ADMIN diffterent filters test
	public void ordersListNasnavAdminDifferentFiltersTest() {
		String token = "101112";
		// no filters
		ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("details_level=3", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals(16, countOrdersFromResponse(response));

		//---------------------------------------------------------------------
		// by org_id
		response = sendOrdersListRequestWithParamsAndToken("org_id=99001&details_level=3", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("8 orders with org_id = 99001",8, countOrdersFromResponse(response));

		//---------------------------------------------------------------------
		// by shop_id
		response = sendOrdersListRequestWithParamsAndToken("shop_id=501&details_level=3", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("3 orders with shop_id = 501",3, countOrdersFromResponse(response));

		//---------------------------------------------------------------------
		// by user_id
		response = sendOrdersListRequestWithParamsAndToken("user_id=88&details_level=3", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("6 orders with user_id = 88",6, countOrdersFromResponse(response));

		//---------------------------------------------------------------------
		// by status
		response = sendOrdersListRequestWithParamsAndToken("status=NEW&details_level=3", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("4 orders with status = NEW", 4, countOrdersFromResponse(response));

		//---------------------------------------------------------------------
		// by org_id and status
		response = sendOrdersListRequestWithParamsAndToken("org_id=99001&status=NEW&details_level=3", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("1 orders with org_id = 99001 and status = NEW",1 , countOrdersFromResponse(response));

		//---------------------------------------------------------------------
		// by org_id and shop_id
		response = sendOrdersListRequestWithParamsAndToken("org_id=99001&shop_id=503&details_level=3", token);

		//---------------------------------------------------------------------
		// by org_id and user_id
		response = sendOrdersListRequestWithParamsAndToken("org_id=99002&user_id=90&details_level=3", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("1 order with org_id = 99002 and user_id = 90", 1, countOrdersFromResponse(response));

		//---------------------------------------------------------------------
		// by shop_id and status
		response = sendOrdersListRequestWithParamsAndToken("shop_id=501&status=NEW&details_level=3", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("1 orders with shop_id = 501 and status = NEW",1, countOrdersFromResponse(response));


		//---------------------------------------------------------------------
		// by user_id, shop_id and status
		response = sendOrdersListRequestWithParamsAndToken("user_id=88&shop_id=501&status=STORE_PREPARED&details_level=3", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("1 order with user_id = 88 and shop_id = 501 and status = NEW",1, countOrdersFromResponse(response));
	}

	@Test
	public void ordersListNasnavAdminVariesStatusesFilterTest() {
		String token = "101112";

		ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("status=NEW,CLIENT_CONFIRMED&details_level=3", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("10 orders with status = NEW and CLIENT_CONFIRMED", 10, countOrdersFromResponse(response));
	}

	@Test // Organization roles diffterent filters test
	public void ordersListOrganizationDifferentFiltersTest() {
		ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("details_level=3", "161718");

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("user#70 is Organization employee in org#99001 so he can view all orderes within org#99001", 8, countOrdersFromResponse(response));
		//-------------------------------------------------------------------------

		response = sendOrdersListRequestWithParamsAndToken("details_level=3", "131415");

		long org99002Orders = orderRepository.countByOrganizationEntity_id(99002L);
		assertTrue(200 == response.getStatusCode().value());
		assertEquals("user#69 is Organization admin in org#99002 so he can view all orderes within org#99002", org99002Orders, countOrdersFromResponse(response));

		//-------------------------------------------------------------------------
		response = sendOrdersListRequestWithParamsAndToken("details_level=3", "192021");

		Long shopEmpId = 71L;
		Long shopId = empRepository.findById(shopEmpId)
									.map(EmployeeUserEntity::getShopId)
									.get();
		long shopOrdersCount = orderRepository.countByShopsEntity_id( shopId );
		assertTrue(200 == response.getStatusCode().value());
		assertEquals( 	format( "user#%d is store employee in store#%d so he can view all orderes within the store", shopEmpId, shopId)
						, shopOrdersCount
						, countOrdersFromResponse(response));
	}
	
	
	
	

	@Test
	public void ordersListUnAuthTest() {
		// invalid user-id test
		ResponseEntity<String> response = template.exchange("/order/list?shop_id=501", GET,
				new HttpEntity<>(getHeaders("NO_EXISATING_TOKEN")), String.class); //no user with id = 99

		assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
	}
	
	
	
	
	

	@Test
	public void ordersListInvalidFiltersTest() {
		String token = "101112";
		// by shop_id only
		ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("shop_id=550", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("No orders with shop_id = 550 ", 0, countOrdersFromResponse(response));

		// by user_id
		response = sendOrdersListRequestWithParamsAndToken("user_id=99", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("no orders with user_id = 99", 0, countOrdersFromResponse(response));

		// by org_id
		response = sendOrdersListRequestWithParamsAndToken("org_id=999999", token);

		assertTrue(200 == response.getStatusCode().value());
		assertEquals("no orders with org_id = 999999", 0, countOrdersFromResponse(response));

		// by status
		response = sendOrdersListRequestWithParamsAndToken("status=invalid_status", token);

		assertEquals(400, response.getStatusCodeValue());
	}

	
	@Test
	public void testDateFilteration() {
		modifyOrderUpdateTime(330044L, LocalDateTime.of(2020, 11, 26, 10, 00, 00));
		modifyOrderUpdateTime(330045L, LocalDateTime.of(2020, 12, 15, 10, 00, 00));
		modifyOrderUpdateTime(330046L, LocalDateTime.of(2020, 12, 16, 10, 00, 00));
		
		//-------------------------------------------------------------------
		// by shop_id only
		ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("updated_before=2020-12-23:12:12:12&updated_after=2020-12-01:12:12:12", "101112");
		
		assertTrue(200 == response.getStatusCode().value());
		assertEquals("expected 2 orders to be within this given time range ", 2, countOrdersFromResponse(response));
	}

	private ResponseEntity sendOrdersListRequestWithParamsAndToken(String params, String token){
		HttpEntity<?> httpEntity = getHttpEntity(token);

		return template.exchange(ORDER_LIST_API_PATH + "?" + params,
				GET,
				httpEntity,
				OrdersListResponse.class);
	}

	private int countOrdersFromResponse(ResponseEntity<OrdersListResponse> response){
		return response.getBody().getOrders().size();
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
	public void getOrderListLevelTwoTest() throws  IOException {

		ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("details_level=2&count=1", "101112");

		DetailedOrderRepObject body = response.getBody().getOrders().get(0);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue( body.getTotalQuantity() != null);
		assertEquals(3L, body.getTotalQuantity().longValue());
		assertEquals(null, body.getItems());
	}


	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getOrderListCountTest() throws  IOException {
		String token = "101112";
		ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("count=1", token);
		OrdersListResponse body = response.getBody();
		long count = body.getOrders().size();

		assertEquals(1, count);

		response = sendOrdersListRequestWithParamsAndToken("count=2", token);
		body = response.getBody();
		count = body.getOrders().size();

		assertEquals(2, count);

		response = sendOrdersListRequestWithParamsAndToken("count=4", token);
		body = response.getBody();
		count = body.getOrders().size();

		assertEquals(4, count);
	}





	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getOrderListStartTest() throws  IOException { //count=1&
		String token = "101112";

		ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("start=1&count=1&details_level=3", token);

		DetailedOrderRepObject body = response.getBody().getOrders().get(0);
		DetailedOrderRepObject expectedBody = createExpectedOrderInfo(330005L, new BigDecimal("50.00"), 1, "NEW", 89L, ZERO, false, new BigDecimal("20.00"));
		assertEquals(expectedBody, body);

		response = sendOrdersListRequestWithParamsAndToken("start=2&count=1&details_level=3", token);

		body = response.getBody().getOrders().get(0);

		expectedBody = createExpectedOrderInfo(330004L, new BigDecimal("200.00"), 5, "NEW", 89L, new BigDecimal("100.00"), false, new BigDecimal("20.00"));
		expectedBody.setAmountPayedFromReferral(new BigDecimal("25.00"));
		expectedBody.setIsPayedFromReferral(true);

		assertEquals(expectedBody, body);

		response = sendOrdersListRequestWithParamsAndToken("start=3&count=1&details_level=3", token);

		body = response.getBody().getOrders().get(0);

		expectedBody = createExpectedOrderInfo(330003L, new BigDecimal("300.00"), 7, "NEW", 88L, ZERO, true, new BigDecimal("20.00"));
		assertEquals(expectedBody, body);

	}

	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getOrderListOrderedByQuantityTest() throws IOException {
		String token = "101112";
		ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("details_level=3&orders_sorting_option=QUANTITY", token);
		List<DetailedOrderRepObject> orders = response.getBody().getOrders();

		Assert.assertEquals(330002L, orders.get(0).getOrderId());
		Assert.assertEquals(330003L, orders.get(1).getOrderId());
	}

	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getOrderListSortingWayTest() throws IOException {
		String token = "101112";
		ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("sorting_way=ASC", token);
		List<DetailedOrderRepObject> orders = response.getBody().getOrders();

		Assert.assertEquals(330002L, orders.get(0).getOrderId());
		Assert.assertEquals(330003L, orders.get(1).getOrderId());

		response = sendOrdersListRequestWithParamsAndToken("sorting_way=DESC", token);
		orders = response.getBody().getOrders();

		Assert.assertEquals(330006L, orders.get(0).getOrderId());
		Assert.assertEquals(330005L, orders.get(1).getOrderId());
	}
	
	private List<DetailedOrderRepObject> getOrderListDetailedObject(ResponseEntity<String> response) throws IOException {
		return mapper
				.readValue(response.getBody(), new TypeReference<List<DetailedOrderRepObject>>() {});
	}

	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_15.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getOrdersFilters(){
		ResponseEntity<OrdersFiltersResponse> response = sendGetFiltersRequest("161718");
		OrdersFiltersResponse responseBody = response.getBody();

		assertFiltersShops(responseBody);
		assertFiltersUsers(responseBody);
		assertFiltersPrices(responseBody);
		assertFiltersQuantities(responseBody);
		assertFiltersDates(responseBody);
	}

	private ResponseEntity<OrdersFiltersResponse> sendGetFiltersRequest(String token){
		return template
					.exchange("/order/filters?details_level=3",
							GET,
							getHttpEntity(token),
							OrdersFiltersResponse.class);
	}

	private void assertFiltersShops(OrdersFiltersResponse responseBody) {
		Set<Long> expectedShopsIds = setOf(501L, 502L);
		Set<ShopRepresentationObject> actualShops = responseBody.getShops();
		List<Long> actualShopsIds = actualShops
										.stream()
										.map(ShopRepresentationObject::getId)
										.collect(toList());
		checkExpectedEqualsActualId(expectedShopsIds, actualShopsIds);
	}

	private void assertFiltersUsers(OrdersFiltersResponse responseBody) {
		Set<Long> expectedUsersIds = setOf(88L, 89L, 90L);
		Set<UserRepresentationObject> actualUsers = responseBody.getUsers();
		List<Long> actualShopsIds = actualUsers
										.stream()
										.map(UserRepresentationObject::getId)
										.collect(toList());
		checkExpectedEqualsActualId(expectedUsersIds, actualShopsIds);
	}

	private void checkExpectedEqualsActualId(Set<Long> expectedIds, List<Long> actualIds){
		expectedIds.forEach(shopId -> {
			if(actualIds.contains(shopId))
				actualIds.remove(shopId);
		});
		assertEquals(0, actualIds.size());
	}

	private void assertFiltersPrices(OrdersFiltersResponse responseBody) {
		assertEquals(new BigDecimal("100.00"), responseBody.getPrices().getMinPrice());
		assertEquals(new BigDecimal("2000.00"), responseBody.getPrices().getMaxPrice());
	}

	private void assertFiltersQuantities(OrdersFiltersResponse responseBody) {
		assertEquals(Integer.valueOf(2), responseBody.getQuantities().getMinQuantity());
		assertEquals(Integer.valueOf(15), responseBody.getQuantities().getMaxQuantity());
	}

	private void assertFiltersDates(OrdersFiltersResponse responseBody) {
		assertEquals("2022-05-01", responseBody.getDates().minCreatedDate);
		assertEquals("2022-06-05", responseBody.getDates().maxCreatedDate);
	}

	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getCurrentOrderNotFoundTest() throws JsonParseException, JsonMappingException, IOException {
			
		ResponseEntity<String> response = template.exchange("/order/current?details_level=3"
														, GET
														, new HttpEntity<>(getHeaders("789"))
														, String.class);
		
		log.debug("Order >>>> {}", response.getBody());	
		
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
	
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getCurrentOrderUserHasNoOrdersTest() throws JsonParseException, JsonMappingException, IOException {
			
		ResponseEntity<String> response = template.exchange("/order/current?details_level=3"
														, GET
														, new HttpEntity<>(getHeaders("011"))
														, String.class);
		
		log.debug("Order >>>> {}", response.getBody());	
		
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
	
	








	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void deleteCurrentOrderNoAuthTest() throws JsonParseException, JsonMappingException, IOException {

		ResponseEntity<String> response = template.exchange("/order/current"
															, HttpMethod.DELETE
															, new HttpEntity<>(getHeaders("NON_EXISTING_TOKEN"))
															, String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}


	
	
	private JSONObject createOrderStatusUpdateRequest(OrderStatus status) {
		JSONObject request = new JSONObject();
		request.put("status", status.name());
		return request;
	}
	
	
	
	

	

	private DetailedOrderRepObject createExpectedOrderInfo(Long orderId, BigDecimal price, Integer quantity
			, String status, Long userId, BigDecimal discount, Boolean isReferralProgram, BigDecimal withdrawAmount) {
		OrdersEntity entity = helper.getOrderEntityFullData(orderId);

		DetailedOrderRepObject order = new DetailedOrderRepObject();
		order.setUserId(userId);
		order.setUserName(entity.getName());
        order.setShopName(entity.getShopsEntity().getName());
		order.setCurrency("EGP");
		order.setCreatedAt( entity.getCreationDate() );
		order.setDeliveryDate( entity.getDeliveryDate() );
		order.setOrderId( orderId );
		order.setShippingAddress( null );
		order.setShopId( entity.getShopsEntity().getId() );
		order.setStatus( status );
		if(isReferralProgram) {
			order.setIsReferralCodeApplied(true);
			order.setAppliedReferralCode("abcdfg");
		}

		OrderUserResponse orderUser= new OrderUserResponse();
		Optional<UserEntity> optionalUser = userRepository.findById(entity.getUserId());
		if (optionalUser.isPresent()) {
			UserEntity user = optionalUser.get();
			orderUser.setId(user.getId());
			orderUser.setFullName(user.getFullName());
			orderUser.setEmail(user.getEmail());
			orderUser.setPhoneNumber(user.getPhoneNumber());
			orderUser.setGender(user.getGender());
			orderUser.setUserStatus(user.getUserStatus());
			orderUser.setImage(user.getImage());
			orderUser.setCreationTime(user.getCreationTime());
			order.setOrderUser(orderUser);
		}

		order.setIsUsedReferralBalance(withdrawAmount.compareTo(ZERO) > 0);
		order.setSubtotal( price );
		order.setTotal( price);		
		order.setItems( createExpectedItems(price, quantity, discount));
		order.setTotalQuantity(quantity);
		order.setPaymentStatus(entity.getPaymentStatus().toString());
		order.setMetaOrderId(310001L);
		order.setDiscount(ZERO);
		order.setPaymentOperator("S.C.A.M");
		order.setNotes("");
		return order;
	}
	
	
	
	
	

	private List<BasketItem> createExpectedItems(BigDecimal price, Integer quantity, BigDecimal discount) {
		BigDecimal discountAmount = discount.divide(new BigDecimal("100")).multiply(price).setScale(0);
		BasketItem item = new BasketItem();
		item.setProductId(1001L);
		item.setName("product_1");
		item.setStockId( 601L );
		item.setQuantity(quantity);
		item.setTotalPrice( price.subtract(discountAmount).multiply(new BigDecimal(quantity)) );
		item.setThumb(EXPECTED_COVER_IMG_URL);
		item.setPrice(price);
		return Arrays.asList(item);
	}
	
	
	
	
	
	@Test
	public void storeManagerUpdateOrderForAnotherStore() {		
		Long orderId = 330036L;
		String userToken = "sdfe47"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderStatusUpdateRequest(STORE_CANCELLED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/status/update"
										, getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		log.debug("----------response-----------------\n{}", updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(NOT_ACCEPTABLE, updateResponse.getStatusCode());
	}
	
	
	
	@Test
	public void orgManagerUpadateOrderForAntherShop() {
		Long orderId = 330043L;
		String userToken = "131415"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderStatusUpdateRequest(STORE_CANCELLED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/status/update"
										, getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		log.debug("----------response-----------------\n{}", updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void orgManagerUpadateOrderForAntherOrg() {
		Long orderId = 330039L;
		String userToken = "131415"; 
		
		//---------------------------------------------------------------

		JSONObject updateRequest = createOrderStatusUpdateRequest(STORE_CANCELLED);
		updateRequest.put("order_id", orderId);
		
		ResponseEntity<String> updateResponse = 
				template.postForEntity("/order/status/update"
										, getHttpEntity(updateRequest.toString(), userToken)
										, String.class);
		log.debug("----------response-----------------\n{}", updateResponse);
		
		//---------------------------------------------------------------
		assertEquals(NOT_ACCEPTABLE, updateResponse.getStatusCode());
	}
	



	@Test
	public void orderStatusUpdateTest(){
		Long orderId = 330033L;
		String userToken = "252627";
		OrderStatus requiredStatus = OrderStatus.DELIVERED;

		checkOrderStatusBeforeUpdate(orderId, requiredStatus);
		//---------------------------------------------------------------
		JSONObject updateRequest = createOrderStatusUpdateRequest(requiredStatus);
		updateRequest.put("order_id", orderId);

		ResponseEntity<String> updateResponse =
				template.postForEntity("/order/status/update"
						, getHttpEntity(updateRequest.toString(), userToken)
						, String.class);
		//---------------------------------------------------------------
		assertEquals(OK, updateResponse.getStatusCode());
		checkOrderStatusAfterUpdate(orderId, requiredStatus);
	}



	private void checkOrderStatusAfterUpdate(Long orderId, OrderStatus requiredStatus) {
		OrdersEntity subOrderAfter = orderRepository.findByIdAndOrganizationEntity_Id(orderId, 99001L).get();
		OrderStatus subOrderStatusAfter = subOrderAfter.getOrderStatus();
		Integer metaOrderStatusAfterVal = subOrderAfter.getMetaOrder().getStatus();
		OrderStatus metaOrderStatusAfter = OrderStatus.findEnum(metaOrderStatusAfterVal);

		assertEquals(requiredStatus, subOrderStatusAfter);
		assertEquals("if all suborders of the meta-order became of the same status, the meta-order should have this status now"
				, requiredStatus, metaOrderStatusAfter);
	}




	private void checkOrderStatusBeforeUpdate(Long orderId, OrderStatus requiredStatus) {
		OrdersEntity subOrderBefore = orderRepository.findByIdAndOrganizationEntity_Id(orderId, 99001L).get();
		OrderStatus subOrderStatusBefore = subOrderBefore.getOrderStatus();
		Integer metaOrderStatusBeforeVal = subOrderBefore.getMetaOrder().getStatus();
		OrderStatus metaOrderStatusBefore = OrderStatus.findEnum(metaOrderStatusBeforeVal);

		assertNotEquals(requiredStatus, subOrderStatusBefore);
		assertNotEquals(requiredStatus, metaOrderStatusBefore);
	}




	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void confirmOrderAuthZTest() {
		HttpEntity<?> request = getHttpEntity("NOT EXISTENT");
		ResponseEntity<String> res = template.postForEntity("/order/confirm?order_id=330031", request, String.class);
		assertEquals(UNAUTHORIZED, res.getStatusCode());
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void confirmOrderAuthNTest() {
		HttpEntity<?> request = getHttpEntity("131415");
		ResponseEntity<String> res = template.postForEntity("/order/confirm?order_id=330031", request, String.class);
		assertEquals(FORBIDDEN, res.getStatusCode());
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void confirmOrderManagerFromAnotherStoreTest() {
		HttpEntity<?> request = getHttpEntity("sdfe47");
		ResponseEntity<String> res = template.postForEntity("/order/confirm?order_id=330031", request, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void confirmOrderNonExistingOrderTest() {
		HttpEntity<?> request = getHttpEntity("sdrf8s");
		ResponseEntity<String> res = template.postForEntity("/order/confirm?order_id=999999", request, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void confirmOrderAlreadyConfrimedTest() {
		HttpEntity<?> request = getHttpEntity("sdfe47");
		ResponseEntity<String> res = template.postForEntity("/order/confirm?order_id=330032", request, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void confirmOrderTest() {
		Long orderId = 330031L;
		OrdersEntity subOrder = orderRepository.findByIdAndShopsEntity_Id(orderId, 501L).get();
		
		assertEquals(FINALIZED.getValue(), subOrder.getStatus());
		assertNotEquals(STORE_CONFIRMED.getValue(), subOrder.getMetaOrder().getStatus());
		assertNull(subOrder.getShipment().getExternalId());
		assertNull(subOrder.getShipment().getTrackNumber());
		assertEquals(DRAFT.getValue(), subOrder.getShipment().getStatus());
		//-------------------------------------------------
		HttpEntity<?> request = getHttpEntity("sdrf8s");
		ResponseEntity<OrderConfirmResponseDTO> res =
				template.postForEntity("/order/confirm?order_id=330031", request, OrderConfirmResponseDTO.class);
		
		//-------------------------------------------------		
		assertEquals(OK, res.getStatusCode());
		assertFalse(res.getBody().getShippingBill().isEmpty());
		
		OrdersEntity subOrderAfter = orderRepository.findByIdAndShopsEntity_Id(orderId, 501L).get();
		assertEquals(STORE_CONFIRMED.getValue(), subOrderAfter.getStatus());
		assertEquals(STORE_CONFIRMED.getValue(), subOrderAfter.getMetaOrder().getStatus());
		assertNotNull(subOrderAfter.getShipment().getExternalId());
		assertNotNull(subOrderAfter.getShipment().getTrackNumber());
		assertEquals(REQUESTED.getValue(), subOrderAfter.getShipment().getStatus());
	}



	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getMetaOrderTest() {
		HttpEntity<?> request = getHttpEntity("123");
		ResponseEntity<Order> res =
				template.exchange("/order/meta_order/info?id=310001", GET, request, Order.class);

		//-------------------------------------------------
		assertEquals(OK, res.getStatusCode());
		Order order = res.getBody();
		assertEquals(2,order.getSubOrders().size());
	}



	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_14.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getMetaOrderTestWithChangedProductData() {
		HttpEntity<?> request = getHttpEntity("123");
		ResponseEntity<Order> res =
				template.exchange("/order/meta_order/info?id=310001", GET, request, Order.class);

		//-------------------------------------------------
		assertEquals(OK, res.getStatusCode());
		Order order = res.getBody();
		assertEquals(2,order.getSubOrders().size());
		assertOriginalItemDataReturned(order);
	}



	private void assertOriginalItemDataReturned(Order order) {
		Set<String> originalNames = setOf("original", "so original");
		Set<String> originalFeatures = setOf("shiny", "brownie");
		boolean hasOriginalName = hasOriginal(order, originalNames);
		boolean hasOriginalFeatures = hasOriginalFeatures(order, originalFeatures);
		assertTrue(hasOriginalFeatures && hasOriginalName);
	}



	private boolean hasOriginal(Order order, Set<String> originalNames) {
		return order
				.getSubOrders()
				.stream()
				.map(SubOrder::getItems)
				.flatMap(List::stream)
				.map(BasketItem::getName)
				.allMatch(originalNames::contains);
	}



	private boolean hasOriginalFeatures(Order order, Set<String> originalFeatures) {
		return order
				.getSubOrders()
				.stream()
				.map(SubOrder::getItems)
				.flatMap(List::stream)
				.map(BasketItem::getVariantFeatures)
				.map(Map::values)
				.flatMap(Collection::stream)
				.anyMatch(originalFeatures::contains);
	}




	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_13.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getMetaOrderWithIrreturnableItemsTest() {
		HttpEntity<?> request = getHttpEntity("123");
		ResponseEntity<Order> res =
				template.exchange("/order/meta_order/info?id=310001", GET, request, Order.class);

		//-------------------------------------------------
		assertEquals(OK, res.getStatusCode());
		Order order = res.getBody();
		assertEquals(2,order.getSubOrders().size());
		List<BasketItem> allItems =
				order
				.getSubOrders()
				.stream()
				.map(SubOrder::getItems)
				.flatMap(List::stream)
				.collect(toList());
		long nonReturnableItemsNum =
				allItems
				.stream()
				.filter(it -> !it.getIsReturnable())
				.count();
		assertEquals(2, allItems.size());
		assertEquals("orders with status STORE_CONFIRMED are not returnable",1, nonReturnableItemsNum);
	}
	



	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_5.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderFinalizeTest() throws BusinessException, MessagingException, IOException {
		Long orderId = 310001L;
		
		//get stocks before
		Item stock1 = getStockItemQty(601L);
		Item stock2 = getStockItemQty(602L);
		//get cart count before
		List<CartItemEntity> cartBefore = cartRepo.findCurrentCartItemsByUser_Id(88L);
		boolean cartHasStk = 
				cartBefore
				.stream()
				.map(CartItemEntity::getStock)
				.map(StocksEntity::getId)
				.anyMatch(stkId -> stkId.equals(602L));
		assertTrue(cartHasStk);
		
		boolean isUsed = usePromoRepo.existsByPromotion_IdAndUser_Id(630002L, 88L);
		assertFalse(isUsed);
		//---------------------------------------------------------------------
		orderService.finalizeOrder(orderId);
		
		//---------------------------------------------------------------------		
		assertStockReduced(stock1, stock2);
		assertCartItemsRemoved();
		assertOrderStatusChanged(orderId);
		assertEmailMethodsCalled();
		assertPromotionUsed(orderId);
	}






	private void assertPromotionUsed(Long orderId) {
		MetaOrderEntity metaOrder = metaOrderRepo.findById(orderId).get();
		Long userId = metaOrder.getUser().getId();
		boolean isUsed = usePromoRepo.existsByPromotion_IdAndUser_Id(630002L, userId);
		assertTrue(isUsed);
	}






	private void assertStockReduced(Item stock1, Item stock2) {
		Item stock1After = getStockItemQty(601L);
		Item stock2After = getStockItemQty(602L);
		
		assertEquals(14, stock1.getQuantity() - stock1After.getQuantity());
		assertEquals(2, stock2.getQuantity() - stock2After.getQuantity());
	}






	private void assertCartItemsRemoved() {
		List<CartItemEntity> cartAfter = cartRepo.findCurrentCartItemsByUser_Id(88L);
		boolean cartHasStkAfer = 
				cartAfter
				.stream()
				.map(CartItemEntity::getStock)
				.map(StocksEntity::getId)
				.anyMatch(stkId -> stkId.equals(602L));
		assertFalse(cartHasStkAfer);
		assertFalse("if the cart had items not in the order, they should remain", cartAfter.isEmpty());
	}






	private void assertOrderStatusChanged(Long orderId) {
		OrdersEntity subOrder1 = orderRepository.findById(330031L).get();
		assertEquals(FINALIZED.getValue(), subOrder1.getStatus());
		
		OrdersEntity subOrder2 = orderRepository.findById(330032L).get();
		assertEquals(FINALIZED.getValue(), subOrder2.getStatus());
		
		MetaOrderEntity metaOrder = metaOrderRepo.findById(orderId).get();
		assertEquals(FINALIZED.getValue(), metaOrder.getStatus());
	}






	private void assertEmailMethodsCalled() throws MessagingException, IOException {
		Mockito
		.verify(mailService, times(1))
		.sendThymeleafTemplateMail(
			Mockito.eq("organization_1")
			, Mockito.eq("user1@nasnav.com")
			, Mockito.eq(String.format(BILL_EMAIL_SUBJECT, "organization_1"))
			, Mockito.anyString()
			, Mockito.anyMap());
		
		Mockito
		.verify(mailService, times(1))
		.sendThymeleafTemplateMail(
			Mockito.eq("organization_1")
			, Mockito.eq(asList("testuser6@nasnav.com"))
			, Mockito.anyString()
			, Mockito.eq(asList("testuser2@nasnav.com"))
			, Mockito.anyString()
			, Mockito.anyMap());
		
		Mockito
		.verify(mailService, times(1))
		.sendThymeleafTemplateMail(
			Mockito.eq("organization_1")
			, Mockito.eq(asList("testuser7@nasnav.com"))
			, Mockito.anyString()
			, Mockito.eq(asList("testuser2@nasnav.com"))
			, Mockito.anyString()
			, Mockito.anyMap());

			Mockito.verifyNoMoreInteractions(mailService);
	}

	@Test
	public void testUserDTOForOderResponse(){
		ResponseEntity<DetailedOrderRepObject> response = template.exchange("/order/info?order_id=330048", GET,
				getHttpEntity("101112"), DetailedOrderRepObject.class);
		assertEquals(200, response.getStatusCodeValue());
		DetailedOrderRepObject responseBody = response.getBody();
		assertNotNull(responseBody);
		OrderUserResponse finalOrderUser = responseBody.getOrderUser();
		Assertions.assertAll("Should pass for userDto in each order",
				() -> assertNotNull(finalOrderUser),
				() -> assertEquals(finalOrderUser.getId(),responseBody.getUserId()),
				() -> assertEquals(Gender.MALE, finalOrderUser.getGender()),
				() -> assertEquals((int)UserStatus.ACTIVATED.getValue(),finalOrderUser.getUserStatus()),
				() -> assertNotNull(finalOrderUser.getFullName()),
				() -> assertNotNull(finalOrderUser.getImage()),
				() -> assertNotNull(finalOrderUser.getEmail()),
				() -> assertNotNull(finalOrderUser.getCreationTime())
		);

		response = template.exchange("/order/info?order_id=330038", GET,
				getHttpEntity("101112"), DetailedOrderRepObject.class);
		assertEquals(200, response.getStatusCodeValue());
		DetailedOrderRepObject nextResponseBody = response.getBody();
		assertNotNull(nextResponseBody);
		OrderUserResponse finalOrderUser2 = nextResponseBody.getOrderUser();

		Assertions.assertAll("Should pass for userDto in each order",
				() -> assertNotNull(finalOrderUser2),
				() -> assertEquals(finalOrderUser2.getId(),nextResponseBody.getUserId()),
				() -> assertEquals(Gender.MALE,finalOrderUser2.getGender()),
				() -> assertEquals((int)UserStatus.ACCOUNT_SUSPENDED.getValue(),finalOrderUser2.getUserStatus()),
				() -> assertNotNull(finalOrderUser2.getFullName()),
				() -> assertNotNull(finalOrderUser2.getImage()),
				() -> assertNotNull(finalOrderUser2.getEmail()),
				() -> assertNotNull(finalOrderUser2.getCreationTime())
		);
	}

	@Test
	public void failTestUserDTOForOderResponse(){
		ResponseEntity<DetailedOrderRepObject> response = template.exchange("/order/info?order_id=330048", GET,
				getHttpEntity("101112"), DetailedOrderRepObject.class);
		assertEquals(200, response.getStatusCodeValue());
		DetailedOrderRepObject responseBody = response.getBody();
		assertNotNull(responseBody);
		OrderUserResponse finalOrderUser = responseBody.getOrderUser();

		Assertions.assertAll("Should pass for userDto in each order",
				() -> assertNotEquals(Gender.FEMALE,finalOrderUser.getGender()),
				() -> assertNotEquals((int)UserStatus.NOT_ACTIVATED.getValue(),finalOrderUser.getUserStatus())
		);

		response = template.exchange("/order/info?order_id=330038", GET,
				getHttpEntity("101112"), DetailedOrderRepObject.class);
		assertEquals(200, response.getStatusCodeValue());
		DetailedOrderRepObject nextResponseBody = response.getBody();
		assertNotNull(nextResponseBody);
		OrderUserResponse finalOrderUser2 = nextResponseBody.getOrderUser();

		Assertions.assertAll("Should pass for userDto in each order",
				() -> assertNotEquals(finalOrderUser2.getId(),responseBody.getUserId()),
				() -> assertNotEquals(Gender.FEMALE,finalOrderUser2.getGender()),
				() -> assertNotEquals((int)UserStatus.ACTIVATED.getValue(),finalOrderUser2.getUserStatus())
		);
	}



	@Test
	public void getAnyOrderNasnavAdminTest() {
		ResponseEntity<DetailedOrderRepObject> response = template.exchange("/order/info?order_id=330048", GET,
																getHttpEntity("101112"), DetailedOrderRepObject.class);
		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getBody()!=null);

		response = template.exchange("/order/info?order_id=330038", GET,
				getHttpEntity("101112"), DetailedOrderRepObject.class);
		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getBody()!=null);
	}

	@Test
	public void getAnyOrderNasnavAdminByUserEmailTest() {
		ResponseEntity<OrdersListResponse> response = template.exchange("/order/infos/user1@nasnav.com", GET,
				getHttpEntity("101112"),
				OrdersListResponse.class);
		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getBody()!=null);
		assertEquals(4 ,response.getBody().getOrders().size());
	}

	@Test
	public void getAnyOrderNasnavAdminByUserEmailTest2() {
		ResponseEntity<OrdersListResponse> response = template.exchange("/order/infos/user1@nasnav.com", GET,
				getHttpEntity("252627"),
				OrdersListResponse.class);
		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getBody()!=null);
		assertEquals(4 ,response.getBody().getOrders().size());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Order_Associated_Promotions.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void getOrderAssociatedPromoCodeIfExist() {
		ResponseEntity<DetailedOrderRepObject> response = template.exchange("/order/info?order_id=1", GET,
				getHttpEntity("123"), DetailedOrderRepObject.class);
		assertEquals(200, response.getStatusCodeValue());
		DetailedOrderRepObject body = response.getBody();
		List<OrderAssociatedPromotions> orderAssociatedPromotions = body.getOrderAssociatedPromotions();
		assertEquals(orderAssociatedPromotions.get(0).getCode(),"GREEEEEED");
	}

	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getAnyMetaOrderNasnavAdminTest() {
		HttpEntity<?> request = getHttpEntity("101112");
		ResponseEntity<Order> res =
				template.exchange("/order/meta_order/info?id=310001", GET, request, Order.class);

		//-------------------------------------------------
		assertEquals(OK, res.getStatusCode());
		Order order = res.getBody();
		assertNotNull(order);
	}


	


	private Item getStockItemQty(Long stockId) {
		return stockRepository
				.findById(stockId)
				.map(stk -> new Item(stk.getId(), stk.getQuantity()))
				.get();
	}


	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getMetaOrderListTest() throws IOException {
		HttpEntity<?> request = getHttpEntity("123");
		ResponseEntity<String> res =
				template.exchange("/order/meta_order/list/user", GET, request, String.class);

		//-------------------------------------------------
		assertEquals(OK, res.getStatusCode());
		List<MetaOrderBasicInfo> orders = mapper.readValue(res.getBody(), new TypeReference<List<MetaOrderBasicInfo>>(){});
		assertEquals(1,orders.size());
	}


	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getMetaOrderListEmployeeTest() throws IOException {
		HttpEntity<?> request = getHttpEntity("131415");
		ResponseEntity<String> res =
				template.exchange("/order/meta_order/list/user", GET, request, String.class);

		//-------------------------------------------------
		assertEquals(FORBIDDEN, res.getStatusCode());
	}
	
	
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/database_cleanup.sql","/sql/Orders_Test_Data_Insert_7.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void rejectOrderAuthZTest() {
		HttpEntity<?> request = getHttpEntity("NOT EXISTENT");
		ResponseEntity<String> res = template.postForEntity("/order/reject", request, String.class);
		assertEquals(UNAUTHORIZED, res.getStatusCode());
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_7.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void rejectOrderAuthNTest() {
		HttpEntity<?> request = getHttpEntity("131415");
		ResponseEntity<String> res = template.postForEntity("/order/reject", request, String.class);
		assertEquals(FORBIDDEN, res.getStatusCode());
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_7.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void rejectOrderManagerFromAnotherStoreTest() {
		Long orderId = 330031L;
		HttpEntity<?> request = createOrderRejectRequest(orderId, "sdfe47");
		ResponseEntity<String> res = template.postForEntity("/order/reject", request, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_7.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void rejectOrderNonExistingOrderTest() {
		HttpEntity<?> request = createOrderRejectRequest(-111L, "sdrf8s");
		ResponseEntity<String> res = template.postForEntity("/order/reject", request, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_7.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void rejectOrderAlreadyConfrimedTest() {
		Long orderId = 330032L;
		HttpEntity<?> request = createOrderRejectRequest(orderId, "sdfe47");
		ResponseEntity<String> res = template.postForEntity("/order/reject", request, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}

	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_7.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void rejectOrderTest() throws Exception {
		Long orderId = 330031L;
		OrdersEntity subOrder = orderRepository.findByIdAndShopsEntity_Id(orderId, 501L).get();
		
		
		//get stocks before
		Item stock1 = getStockItemQty(601L);
		Item stock2 = getStockItemQty(602L);
		
		assertEquals(FINALIZED.getValue(), subOrder.getStatus());
		assertEquals(FINALIZED.getValue(), subOrder.getMetaOrder().getStatus());
		assertNull(subOrder.getShipment().getExternalId());
		assertNull(subOrder.getShipment().getTrackNumber());
		assertEquals(DRAFT.getValue(), subOrder.getShipment().getStatus());
		//-------------------------------------------------
		HttpEntity<?> request = createOrderRejectRequest(orderId, "Oops!", "sdrf8s");
		ResponseEntity<?> res = 
				template.postForEntity("/order/reject", request, String.class);
		
		//-------------------------------------------------		
		assertEquals(OK, res.getStatusCode());
		
		OrdersEntity subOrderAfter = orderRepository.findByIdAndShopsEntity_Id(orderId, 501L).get();
		assertEquals(STORE_CANCELLED.getValue(), subOrderAfter.getStatus());
		assertEquals(STORE_CANCELLED.getValue(), subOrderAfter.getMetaOrder().getStatus());
		assertNull(subOrderAfter.getShipment().getExternalId());
		assertNull(subOrderAfter.getShipment().getTrackNumber());
		assertEquals(DRAFT.getValue(), subOrderAfter.getShipment().getStatus());
		
		//-------------------------------------------------	
		//assert stock incremented
		Item stock1After = getStockItemQty(601L);
		Item stock2After = getStockItemQty(602L);
		
		assertEquals(14, stock1After.getQuantity() - stock1.getQuantity() );
		assertEquals("the other sub-order should remain the same", 0, stock2After.getQuantity() - stock2.getQuantity());
		//-------------------------------------------------	
		//assert email methods called
		Mockito
		.verify(mailService)
		.sendThymeleafTemplateMail(
			Mockito.eq("organization_1")
			,Mockito.eq(asList("user1@nasnav.com"))
			, Mockito.eq(format(ORDER_REJECT_SUBJECT, "organization_1" , orderId ))
			, Mockito.anyList()
			, Mockito.eq(asList("testuser3@nasnav.com"))
			, Mockito.eq(ORDER_REJECT_TEMPLATE)
			, Mockito.anyMap());
	}

	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderCancelNoAuthZTest() {
		HttpEntity<?> request = getHttpEntity("NOT EXISTENT");
		ResponseEntity<String> res = template.postForEntity("/order/cancel?meta_order_id=310001", request, String.class);
		assertEquals(UNAUTHORIZED, res.getStatusCode());
	}



	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderCancelNoAuthNTest() {
		HttpEntity<?> request = getHttpEntity("131415");
		ResponseEntity<String> res = template.postForEntity("/order/cancel?meta_order_id=310001", request, String.class);
		assertEquals(FORBIDDEN, res.getStatusCode());
	}
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderCancelAnotherCustomerTest() {
		HttpEntity<?> request = getHttpEntity("666");
		ResponseEntity<String> res = template.postForEntity("/order/cancel?meta_order_id=310001", request, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderCancelNoExistingOrderTest() {
		HttpEntity<?> request = getHttpEntity("123");
		ResponseEntity<String> res = template.postForEntity("/order/cancel?meta_order_id=313331", request, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_8.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderCancelNotFinalizedTest() {
		HttpEntity<?> request = getHttpEntity("123");
		ResponseEntity<String> res = template.postForEntity("/order/cancel?meta_order_id=310003", request, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_8.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderCancelSuccessTest() throws Exception {
		Long metaOrderId = 310001L;
		MetaOrderEntity metaOrderBefore = metaOrderRepo.findFullDataById(metaOrderId).get();
		boolean allSubOrdersAreFinalized = areAllSubOrdersHaveStatus(metaOrderBefore, FINALIZED);
		
		Integer stock1Before = stockRepository.findById(601L).get().getQuantity();
		Integer stock2Before = stockRepository.findById(602L).get().getQuantity();
		
		assertEquals(FINALIZED.getValue(), metaOrderBefore.getStatus());
		assertTrue(allSubOrdersAreFinalized);
		//------------------------------------------------------------
		HttpEntity<?> request = getHttpEntity("123");
		ResponseEntity<String> res = template.postForEntity("/order/cancel?meta_order_id="+metaOrderId, request, String.class);
		assertEquals(OK, res.getStatusCode());
		//------------------------------------------------------------
		
		MetaOrderEntity metaOrderAfter = metaOrderRepo.findFullDataById(metaOrderId).get();
		boolean allSubOrdersAreCancelled = areAllSubOrdersHaveStatus(metaOrderAfter, CLIENT_CANCELLED);
		
		Integer stock1After = stockRepository.findById(601L).get().getQuantity();
		Integer stock2After = stockRepository.findById(602L).get().getQuantity();
		
		assertEquals(CLIENT_CANCELLED.getValue(), metaOrderAfter.getStatus());
		assertTrue(allSubOrdersAreCancelled);
		assertEquals("stocks should be incremented", 14, stock1After - stock1Before);
		assertEquals("stocks should be incremented", 2, stock2After - stock2Before);
		//------------------------------------------------------------

		Mockito
		.verify(mailService)
		.sendThymeleafTemplateMail(
			Mockito.eq("organization_1")
			, Mockito.eq(asList("testuser6@nasnav.com"))
			, Mockito.anyString()
			, Mockito.eq(asList("testuser2@nasnav.com"))
			, Mockito.anyString()
			, Mockito.anyMap());
		
		Mockito
		.verify(mailService)
		.sendThymeleafTemplateMail(
			Mockito.eq("organization_1")
			, Mockito.eq(asList("testuser7@nasnav.com"))
			, Mockito.anyString()
			, Mockito.eq(asList("testuser2@nasnav.com"))
			, Mockito.anyString()
			, Mockito.anyMap());
	}



	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_8.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void referralWalletAmountReturnedWhenUserCancelOrder() throws Exception {
		Long metaOrderId = 310001L;
		HttpEntity<?> request = getHttpEntity("123");
		ResponseEntity<String> res = template.postForEntity("/order/cancel?meta_order_id="+metaOrderId, request, String.class);
		assertEquals(OK, res.getStatusCode());

		ReferralWallet referralWallet = referralWalletRepository.findByUserIdAndReferralType(88L, ReferralType.USER).get();
		assertEquals(new BigDecimal("70.00"), referralWallet.getBalance());

	}

	@Test(expected = BusinessException.class)
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_8.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void validateOrderForPaymentOrderIdInvalid() throws BusinessException {
		orderService.validateOrderForPaymentCoD(1L);
	}

	@Test(expected = BusinessException.class)
	@Transactional
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_8.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void validateOrderForPaymentOrderOrganizationCodPaymentNotAvailable() throws BusinessException {
		orderService.validateOrderForPaymentCoD(310002L);
	}

	@Test(expected = BusinessException.class)
	@Transactional
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_16.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void validateOrderForPaymentOrderOrganizationOneOfOrderIsForPickup() throws BusinessException {
		orderService.validateOrderForPaymentCoD(310002L);
	}

	@Test(expected = BusinessException.class)
	@Transactional
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_8.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void validateOrderForPaymentOrderOrganizationPaymentCreated() throws BusinessException {
		PaymentEntity payment = orderService.validateOrderForPaymentCoD(310002L);
		assertEquals("COD", payment.getOperator());
		assertEquals(PaymentStatus.COD_REQUESTED, payment.getStatus());
	}


	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_8.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderCancelWithClientConfirmedStatusSuccessTest() throws Exception {
		Long metaOrderId = 310002L;
		MetaOrderEntity metaOrderBefore = metaOrderRepo.findFullDataById(metaOrderId).get();
		boolean allSubOrdersAreConfirmed = areAllSubOrdersHaveStatus(metaOrderBefore, CLIENT_CONFIRMED);
		
		Integer stock1Before = stockRepository.findById(601L).get().getQuantity();
		Integer stock2Before = stockRepository.findById(602L).get().getQuantity();
		
		assertEquals(CLIENT_CONFIRMED.getValue(), metaOrderBefore.getStatus());
		assertTrue(allSubOrdersAreConfirmed);
		//------------------------------------------------------------
		HttpEntity<?> request = getHttpEntity("123");
		ResponseEntity<String> res = template.postForEntity("/order/cancel?meta_order_id="+metaOrderId, request, String.class);
		assertEquals(OK, res.getStatusCode());
		//------------------------------------------------------------
		
		MetaOrderEntity metaOrderAfter = metaOrderRepo.findFullDataById(metaOrderId).get();
		boolean allSubOrdersAreCancelled = areAllSubOrdersHaveStatus(metaOrderAfter, CLIENT_CANCELLED);
		
		Integer stock1After = stockRepository.findById(601L).get().getQuantity();
		Integer stock2After = stockRepository.findById(602L).get().getQuantity();
		
		assertEquals(CLIENT_CANCELLED.getValue(), metaOrderAfter.getStatus());
		assertTrue(allSubOrdersAreCancelled);
		assertEquals("stocks should be the same if order was still CLIENT_CONFIRMED", 0, stock1After - stock1Before);
		assertEquals("stocks should be the same if order was still CLIENT_CONFIRMED", 0, stock2After - stock2Before);
		//------------------------------------------------------------

		Mockito
		.verify(mailService)
		.sendThymeleafTemplateMail(
			Mockito.eq("organization_1")
			, Mockito.eq(asList("testuser6@nasnav.com"))
			, Mockito.anyString()
			, Mockito.eq(asList("testuser2@nasnav.com"))
			, Mockito.anyString()
			, Mockito.anyMap());
		
		Mockito
		.verify(mailService)
		.sendThymeleafTemplateMail(
			Mockito.eq("organization_1")
			, Mockito.eq(asList("testuser7@nasnav.com"))
			, Mockito.anyString()
			, Mockito.eq(asList("testuser2@nasnav.com"))
			, Mockito.anyString()
			, Mockito.anyMap());
	}






	private boolean areAllSubOrdersHaveStatus(MetaOrderEntity metaOrder, OrderStatus initialStatus) {
		return metaOrder
		.getSubOrders()
		.stream()
		.map(OrdersEntity::getStatus)
		.allMatch(status -> status.equals(initialStatus.getValue()));
	}



	private HttpEntity<?> createOrderRejectRequest(Long orderId, String authToken) {
		return createOrderRejectRequest(orderId, null, authToken);
	}
	
	
	
	private HttpEntity<?> createOrderRejectRequest(Long orderId, String rejectionReason, String authToken) {
		String requestBody = 
				json()
				.put("sub_order_id", orderId)
				.put("rejection_reason", rejectionReason)
				.toString();
		return getHttpEntity(requestBody, authToken);
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
