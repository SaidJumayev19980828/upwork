package com.nasnav.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationCartOptimizationRepository;
import com.nasnav.dao.ReferralTransactionRepository;
import com.nasnav.dao.ReferralWalletRepository;
import com.nasnav.dao.UserOtpRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dao.WishlistItemRepository;
import com.nasnav.dto.BasketItem;
import com.nasnav.dto.response.OrderConfirmResponseDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.dto.response.navbox.Shipment;
import com.nasnav.dto.response.navbox.SubOrder;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.ErrorResponseDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationCartOptimizationEntity;
import com.nasnav.persistence.ReferralTransactions;
import com.nasnav.persistence.ReferralWallet;
import com.nasnav.persistence.ShipmentEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserOtpEntity;
import com.nasnav.service.CartService;
import com.nasnav.service.MailService;
import com.nasnav.service.OrderService;
import com.nasnav.service.impl.CartServiceImpl;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.MessagingException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.enumerations.OrderStatus.CLIENT_CONFIRMED;
import static com.nasnav.enumerations.OrderStatus.DISCARDED;
import static com.nasnav.enumerations.OrderStatus.STORE_CANCELLED;
import static com.nasnav.enumerations.OrderStatus.STORE_CONFIRMED;
import static com.nasnav.service.cart.optimizers.CartOptimizationStrategy.WAREHOUSE;
import static com.nasnav.service.helpers.CartServiceHelper.ADDITIONAL_DATA_PRODUCT_ID;
import static com.nasnav.service.helpers.CartServiceHelper.ADDITIONAL_DATA_PRODUCT_TYPE;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.SERVICE_ID;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class CartTest extends AbstractTestWithTempBaseDir {

	public static final String USELESS_NOTE = "come after dinner";
	@Autowired
    private TestRestTemplate template;

	@Autowired
	private CartItemRepository cartItemRepo;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrdersRepository orderRepo;
	
	@Autowired
	private MetaOrderRepository metaOrderRepo;
	
	@Autowired
	private OrganizationCartOptimizationRepository orgOptimizerRepo;

	@Autowired
	private BasketRepository basketRepo;

	@Autowired
	private WishlistItemRepository wishlistRepo;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ReferralWalletRepository referralWalletRepo;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private EmployeeUserRepository empRepo;

	@Autowired
	private UserOtpRepository userOtpRepository;

	@Mock
	private CartServiceImpl cartServiceMock;

	@Autowired
	private ReferralTransactionRepository referralTransactionRepository;

	@MockBean
	private MailService mailService;

	@Test
	public void getCartNoAuthz() {
        HttpEntity<?> request =  getHttpEntity("NOT FOUND");
        ResponseEntity<Cart> response =
        		template.exchange("/cart", GET, request, Cart.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	public void getCartNoToken() {
        ResponseEntity<Cart> response =
        		template.getForEntity("/cart", Cart.class);

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

	@Test
	public void getCartWithUserIdSuccess() {
		HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<Cart> response =
        		template.exchange("/cart/"+88L, GET, request, Cart.class);

		Cart cart = response.getBody();

        assertEquals(OK, response.getStatusCode());
        assertEquals(2, cart.getItems().size());
        assertProductNamesReturned(response);
		assertEquals(cartService.calculateCartTotal(cart), cart.getSubtotal());
		assertEquals(cart.getSubtotal().subtract(cart.getDiscount()), cart.getTotal());
	}

	@Test
	public void getCartWithUserIdFromOtherOrg() {
		HttpEntity<?> request =  getHttpEntity("5289361");
		ResponseEntity<ErrorResponseDTO> response =
				template.exchange("/cart/"+88L, GET, request, ErrorResponseDTO.class);

		ErrorResponseDTO error = response.getBody();

		assertEquals(NOT_FOUND, response.getStatusCode());
		assertEquals(ErrorCodes.E$USR$0002.name(), error.getError());
	}

	@Test
	public void checkRoleUserToGetCartWithUserIdSuccess() {
		HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Cart> response =
        		template.exchange("/cart/"+88L, GET, request, Cart.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
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

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void addCartItemWithAdditionalData(){
		Long userId = 88L;
		Long stockId = 606L;
		Integer quantity = 1;
		int collectionId = 1009;
		int productType = 2;
		Long itemsCountBefore = cartItemRepo.countByUser_Id(userId);

		JSONObject additionalData =
				json()
				.put(ADDITIONAL_DATA_PRODUCT_ID, collectionId)
				.put(ADDITIONAL_DATA_PRODUCT_TYPE, productType);
		JSONObject itemJson = createCartItem(stockId, quantity);
		itemJson.put("additional_data", additionalData);

		HttpEntity<?> request =  getHttpEntity(itemJson.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(200, response.getStatusCodeValue());
		assertEquals(itemsCountBefore + 1 , response.getBody().getItems().size());

		CartItem item = getCartItemOfStock(stockId, response);

		assertEquals(additionalData.toMap().keySet(), item.getAdditionalData().keySet());
		assertEquals(new HashSet<>(additionalData.toMap().values()), new HashSet<>(item.getAdditionalData().values()));
		assertEquals(collectionId, item.getProductId().intValue());
		assertEquals(productType, item.getProductType().intValue());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void addCartItemWithAdditionalDataAddedToMainJson(){
		Long userId = 88L;
		Long stockId = 606L;
		Integer quantity = 1;
		int collectionId = 1009;
		int productType = 2;
		Long itemsCountBefore = cartItemRepo.countByUser_Id(userId);

		JSONObject additionalData =	json();
		JSONObject itemJson = createCartItem(stockId, quantity);
		itemJson
			.put("additional_data", additionalData)
			.put("product_id", collectionId)
			.put("product_type", productType);

		HttpEntity<?> request =  getHttpEntity(itemJson.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(200, response.getStatusCodeValue());
		assertEquals(itemsCountBefore + 1 , response.getBody().getItems().size());

		CartItem item = getCartItemOfStock(stockId, response);

		Set<String> expectedAdditionalDataFields = setOf(ADDITIONAL_DATA_PRODUCT_ID, ADDITIONAL_DATA_PRODUCT_TYPE);
		Set<Integer> expectedAdditionalDataValues = setOf(collectionId, productType);
		assertEquals(expectedAdditionalDataFields, item.getAdditionalData().keySet());
		assertEquals(expectedAdditionalDataValues, new HashSet<>(item.getAdditionalData().values()));
		assertEquals(collectionId, item.getProductId().intValue());
		assertEquals(productType, item.getProductType().intValue());
	}

	private CartItem getCartItemOfStock(Long stockId, ResponseEntity<Cart> response) {
		return	response
				.getBody()
				.getItems()
				.stream()
				.filter(it -> it.getStockId().equals(stockId))
				.findFirst()
				.get();
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
	public void addCartItemsTest() {
		List<JSONObject> items = createCartItems();

		HttpEntity<?> request =  getHttpEntity(items.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/items", POST, request, Cart.class);

		assertEquals(200, response.getStatusCodeValue());
		assertEquals(2 , response.getBody().getItems().size());
		response.getBody().getItems().stream().forEach(i -> assertTrue(asList(606, 607).contains(i.getStockId().intValue())));

	}

	@Test
	public void addCartItemsNoAuthz() {
		List<JSONObject> items = createCartItems();
		HttpEntity<?> request =  getHttpEntity(items.toString(), "NOT FOUND");
		ResponseEntity<Cart> response =
				template.exchange("/cart/items", POST, request, Cart.class);

		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	public void addCartItemsNoAuthN() {
		List<JSONObject> items = createCartItems();
		HttpEntity<?> request =  getHttpEntity(items.toString(), "101112");
		ResponseEntity<Cart> response =
				template.exchange("/cart/items", POST, request, Cart.class);

		assertEquals(FORBIDDEN, response.getStatusCode());
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
		//Test
		Long itemsCountBefore = cartItemRepo.countByUser_Id(88L);
		Long itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
		HttpEntity<?> request =  getHttpEntity("123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item?item_id=" + itemId, DELETE, request, Cart.class);

		assertEquals(OK, response.getStatusCode());
		assertEquals(itemsCountBefore - 1 , response.getBody().getItems().size());
	}

	@Test
	public void deleteCartItemWithEmployeeTokenAndException() {
		//Test
		Long itemsCountBefore = cartItemRepo.countByUser_Id(88L);
		Long itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
		HttpEntity<?> request =  getHttpEntity("101112");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item?item_id=" + itemId, DELETE, request, Cart.class);

		assertEquals(FORBIDDEN, response.getStatusCode());
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

	private JSONObject createCartItem(Long stockId, Integer quantity) {
		JSONObject item = new JSONObject();
		item.put("stock_id", stockId);
		item.put("cover_img", "img");
		item.put("quantity", quantity);

		return item;
	}

	private List<JSONObject> createCartItems() {
		List<JSONObject> items = new ArrayList<>();

		items.add(createCartItem(607L, 1));
		items.add(createCartItem(606L, 2));

		return items;
	}
	
	private JSONObject createCartItem() {
		return createCartItem(606L, 1);
	}

	@Test
	public void checkoutCartSuccess() {
		checkoutCart();
	}

	@Test
	public void checkoutCartSuccessNewLoyaltyModule() {
			JSONObject requestBody = createCartCheckoutBody();
			requestBody.put("requestedPoints", "100");
			Order body = checkOutCart(requestBody, new BigDecimal("3136.71"), new BigDecimal("3100") ,new BigDecimal("51"));
			assertEquals(new BigDecimal("14.290"),body.getDiscount());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_9.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithDiscounts() {
		JSONObject requestBody = createCartCheckoutBody();
		
		checkOutCart(requestBody, new BigDecimal("5891"), new BigDecimal("5840") ,new BigDecimal("51"));
	}

	@ParameterizedTest
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Referral_Code.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	@CsvSource({"abcdfg, 93.00, 3058"})
	void checkoutCartWithReferralCodeDiscount(String code, BigDecimal expectedDiscount, BigDecimal expectedTotal) {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("referralCode", code);

		BigDecimal exceptedDiscount = expectedDiscount;

		Order order = checkOutCart(requestBody,expectedTotal, new BigDecimal("3100") ,new BigDecimal("51"));

		assertEquals(0, exceptedDiscount.compareTo(order.getDiscount()));
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Referral_Code_1.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithReferralCodeDiscountNotAppliedOutOfRangeDate() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("referralCode", "abcdfg");

		BigDecimal exceptedDiscount = BigDecimal.ZERO;

		Order order = checkOutCart(requestBody, new BigDecimal("3151"), new BigDecimal("3100") ,new BigDecimal("51"));

		assertEquals(0, exceptedDiscount.compareTo(order.getDiscount()));
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Referral_Code_3.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithReferralCodeDiscountNotAppliedAsNotTheUserReferralCode() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("referralCode", "abcdfg");

		BigDecimal exceptedDiscount = BigDecimal.ZERO;

		Order order = checkOutCart(requestBody, new BigDecimal("3151"), new BigDecimal("3100") ,new BigDecimal("51"));

		assertEquals(0, exceptedDiscount.compareTo(order.getDiscount()));
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Referral_Code.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void discountAppliedAndWithdrawValueOnOrderWhenCheckingOutAndApplyPayFromReferral() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("payFromReferralBalance", true);

		BigDecimal exceptedDiscount = new BigDecimal("3151.00");

		Order order = checkOutCartForPayFromReferralBalance(requestBody, new BigDecimal("0.00"), new BigDecimal("3100") ,new BigDecimal("51"));
		BigDecimal subOrdersDiscounts = order.getSubOrders().stream()
				.map(SubOrder::getDiscount)
				.reduce(ZERO, BigDecimal::add);


		assertEquals(0, exceptedDiscount.compareTo(order.getDiscount()));
		assertEquals(0, exceptedDiscount.compareTo(subOrdersDiscounts));

		MetaOrderEntity metaOrderEntity = metaOrderRepo.findById(order.getOrderId()).get();
		assertEquals(0, exceptedDiscount.compareTo(metaOrderEntity.getReferralWithdrawAmount()));

		List<ReferralTransactions> referralTransactions = referralTransactionRepository.findByOrderId(metaOrderEntity.getId());
		assertEquals(1, referralTransactions.size());
		assertEquals(ReferralTransactionsType.ORDER_WITHDRAWAL, referralTransactions.get(0).getType());
		assertEquals(exceptedDiscount, referralTransactions.get(0).getAmount());

		ReferralWallet referralWallet = referralWalletRepo.findById(500L).get();
		assertEquals(0, referralWallet.getBalance().compareTo(new BigDecimal("849.00")));
	}

	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Referral_Code_Insufficient.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void discountNotAppliedAndWithdrawValueOnOrderWhenCheckingOutAndApplyPayFromReferral() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("payFromReferralBalance", true);
		checkOutCartForPayFromReferralBalanceThrowsInsufficient(requestBody, new BigDecimal("0.00"), new BigDecimal("3100") ,new BigDecimal("51"));
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Referral_Code_4.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void withdrawDiscountNotAppliedWhenThereIsNoReferralSettingsForAnOrganization() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("payFromReferralBalance", true);

		BigDecimal exceptedDiscount = ZERO;

		Order order = checkOutCartForPayFromReferralBalance(requestBody, new BigDecimal("3151"), new BigDecimal("3100") ,new BigDecimal("51"));

		BigDecimal subOrdersDiscounts = order.getSubOrders().stream()
				.map(SubOrder::getDiscount)
				.reduce(ZERO, BigDecimal::add);


		assertEquals(0, exceptedDiscount.compareTo(order.getDiscount()));
		assertEquals(0, exceptedDiscount.compareTo(subOrdersDiscounts));

	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Referral_Code_2.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void discountAppliedAndWithdrawValueOnOrderWhenCheckingOutAndApplyPayFromReferralOutDateOfRange() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("payFromReferralBalance", true);

		BigDecimal exceptedDiscount = ZERO;

		Order order = checkOutCartForPayFromReferralBalance(requestBody, new BigDecimal("3151"), new BigDecimal("3100") ,new BigDecimal("51"));

		assertEquals(0, exceptedDiscount.compareTo(order.getDiscount()));

		MetaOrderEntity metaOrderEntity = metaOrderRepo.findById(order.getOrderId()).get();
		assertEquals(0, exceptedDiscount.compareTo(metaOrderEntity.getReferralWithdrawAmount()));

		List<ReferralTransactions> referralTransactions = referralTransactionRepository.findByOrderId(metaOrderEntity.getId());
		assertEquals(0, referralTransactions.size());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Influencer_Referral_Code.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithInfluencerReferralCodeDiscount() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "abcdfg");

		BigDecimal exceptedDiscount = new BigDecimal("20.00");

		Order order = checkOutCart(requestBody, new BigDecimal("3131"), new BigDecimal("3100") ,new BigDecimal("51"));

		assertEquals(0, exceptedDiscount.compareTo(order.getDiscount()));
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Influencer_Referral_Code_1.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithInfluencerReferralCodeDiscountPercentage() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "abcdfg");

		BigDecimal exceptedDiscount = new BigDecimal("62.00");

		Order order = checkOutCart(requestBody, new BigDecimal("3089"), new BigDecimal("3100") ,new BigDecimal("51"));

		assertEquals(0, exceptedDiscount.compareTo(order.getDiscount()));
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Influencer_Referral_Code_4.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithInfluencerReferralCodeDiscountZeroDateOutOfRange() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "abcdfg");

		BigDecimal exceptedDiscount = ZERO;

		Order order = checkOutCart(requestBody, new BigDecimal("3151"), new BigDecimal("3100") ,new BigDecimal("51"));

		assertEquals(0, exceptedDiscount.compareTo(order.getDiscount()));
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Influencer_Referral_Code_2.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithInfluencerReferralCodeThatDoesNotExist() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "abcdfg");

		Order order = checkOutCart(requestBody, new BigDecimal("3151"), new BigDecimal("3100") ,new BigDecimal("51"));

		assertEquals(ZERO, order.getDiscount());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Influencer_Referral_Code_3.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithNoInfluencerReferralDiscountSettings() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "abcdfg");

		Order order = checkOutCart(requestBody, new BigDecimal("3151"), new BigDecimal("3100") ,new BigDecimal("51"));

		assertEquals(ZERO, order.getDiscount().stripTrailingZeros());
	}


	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_9.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithPromotions() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "GREEEEEED");
		
		Order order = checkOutCart(requestBody, new BigDecimal("5790.45"), new BigDecimal("5840") ,new BigDecimal("51"));
		
		MetaOrderEntity entity = metaOrderRepo.findByMetaOrderId(order.getOrderId()).get();
		Long promoId = 	entity.getPromotions().stream().findFirst().get().getId();
				
		assertEquals(630002L, promoId.longValue());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_9.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithUsedPromotions() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "GREEEEEED_HEART");
		
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", request, Order.class);
		
		assertEquals("if promocode was already used, checkout will fail", 406, res.getStatusCodeValue());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_9.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutWithEmployeeTokenWithoutCustomerIdAndException() {
		JSONObject body = new JSONObject();
		Map<String, String> additionalData = new HashMap<>();
		body.put("customer_address", 12300001);
		body.put("shipping_service_id", SERVICE_ID);
		body.put("additional_data", additionalData);
		body.put("notes", "come after dinner");


		HttpEntity<?> request = getHttpEntity(body.toString(), "101112");
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", request, Order.class);
		assertEquals(404, res.getStatusCodeValue());
	}


	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_9.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithPromotionsWithDifferentCase() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "GREEEEEED");
		
		Order order = checkOutCart(requestBody, new BigDecimal("5790.45"), new BigDecimal("5840") ,new BigDecimal("51"));
		
		MetaOrderEntity entity = metaOrderRepo.findByMetaOrderId(order.getOrderId()).get();
		Long promoId = 	entity.getPromotions().stream().findFirst().get().getId();
				
		assertEquals(630002L, promoId.longValue());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_9.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithPromotionsWithPercentage() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "MORE_GREEEEEEED");
		
		Order order = checkOutCart(requestBody, new BigDecimal("5249.18"), new BigDecimal("5840") ,new BigDecimal("51"));
		
		MetaOrderEntity entity = metaOrderRepo.findByMetaOrderId(order.getOrderId()).get();
		Long promoId = 	entity.getPromotions().stream().findFirst().get().getId();
				
		assertEquals(630003L, promoId.longValue());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_9.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithPromotionsWithPercentageButBelowMinCartValue() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "SCAM_GREEEEEEED");

		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", request, Order.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_9.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithPromotionsWithPercentageButWithTooHighDiscount() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "kafa");

		Order order = checkOutCart(requestBody, new BigDecimal("5881"), new BigDecimal("5840") ,new BigDecimal("51"));

		MetaOrderEntity entity = metaOrderRepo.findByMetaOrderId(order.getOrderId()).get();
		Long promoId = 	entity.getPromotions().stream().findFirst().get().getId();

		assertEquals(630006L, promoId.longValue());
	}

	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_14.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void calcPromoDiscountWithApplicableUser() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "kafa_0");

		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Object> res = template.postForEntity("/cart/checkout", request, Object.class);
		assertEquals(OK, res.getStatusCode());
	}

	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_14.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void calcPromoDiscountWithNotApplicableUser() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "kafa_00");

		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Object> res = template.postForEntity("/cart/checkout", request, Object.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}

	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_14.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void calcPromoDiscountFromSpecificBrandsWithApplicableUser() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "kafa_7");

		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Object> res = template.postForEntity("/cart/checkout", request, Object.class);
		assertEquals(OK, res.getStatusCode());
	}

	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_14.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void calcPromoDiscountFromSpecificBrandsWithNotApplicableUser() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "kafa_77");

		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Object> res = template.postForEntity("/cart/checkout", request, Object.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}

	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_14.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void calcPromoDiscountFromSpecificTagsWithApplicableUser() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "kafa_8");

		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Object> res = template.postForEntity("/cart/checkout", request, Object.class);
		assertEquals(OK, res.getStatusCode());
	}

	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_14.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void calcPromoDiscountFromSpecificTagsWithNotApplicableUser() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "kafa_88");

		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Object> res = template.postForEntity("/cart/checkout", request, Object.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}

	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_14.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void calcPromoDiscountFromSpecificProductsWithApplicableUser() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "kafa_9");

		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Object> res = template.postForEntity("/cart/checkout", request, Object.class);
		assertEquals(OK, res.getStatusCode());
	}

	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_14.sql"})
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void calcPromoDiscountFromSpecificProductsWithNotApplicableUser() {
		JSONObject requestBody = createCartCheckoutBody();
		requestBody.put("promo_code", "kafa_99");

		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Object> res = template.postForEntity("/cart/checkout", request, Object.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}

	//@Test test ignored as auto optimization changes are not acceptable now
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_7.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithAutoOptimizationSuccess() {
		JSONObject requestBody = createCartCheckoutBody();
		Order order = checkOutCart(requestBody, new BigDecimal("8151"), new BigDecimal("8100") ,new BigDecimal("51"));
		
		List<BasketItem> items = 
				order
				.getSubOrders()
				.stream()
				.map(SubOrder::getItems)
				.flatMap(List::stream)
				.collect(toList());
		List<Long> orderStocks = items.stream().map(BasketItem::getStockId).collect(toList());
		assertEquals(3, items.size());
		assertTrue("The optimization should pick 2 stocks from a shop in cairo that has the largest average stock quantity, "
				+ "and the third remaining stock from another shop in cairo with less stock quantity."
				+ " as the prices didn't change, the optimization will be done silently."
					, asList(607L, 608L, 612L).stream().allMatch(orderStocks::contains));
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_8.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutCartWithFailedOptimization() {
		JSONObject requestBody = createCartCheckoutBody();
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", request, Order.class);
		
		assertEquals("failure in optimization due to different prices will return an error.", 406, res.getStatusCodeValue());
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

        assertNotNull(order.getOrderId());
		assertEquals(0 ,shippingFee.compareTo(order.getShipping()));
		assertEquals(0 ,subTotal.compareTo(order.getSubtotal()));
		assertEquals(0 ,total.compareTo(order.getTotal()));
		assertEquals(0 ,order.getShipping().compareTo(subOrderShippingSum));
		assertEquals(0 ,order.getSubtotal().compareTo(subOrderSubtTotalSum));
		assertEquals(USELESS_NOTE, order.getNotes());
		assertItemDataJsonCreated(order);
		return order;
	}

	private Order checkOutCartForPayFromReferralBalance(JSONObject requestBody, BigDecimal total, BigDecimal subTotal, BigDecimal shippingFee) {
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", request, Order.class);
		assertEquals(200, res.getStatusCodeValue());

		Order order =  res.getBody();
		BigDecimal subOrderSubTotalSum = getSubOrderSubTotalSum(order);
		BigDecimal subOrderShippingSum = getSubOrderShippingSum(order);

        assertNotNull(order.getOrderId());
		assertEquals(0 ,shippingFee.compareTo(order.getShipping()));
		assertEquals(0 ,subTotal.compareTo(order.getSubtotal()));
		assertEquals(0 ,total.compareTo(order.getTotal()));
		assertEquals(0 ,order.getShipping().compareTo(subOrderShippingSum));
		assertEquals(0 ,order.getSubtotal().compareTo(subOrderSubTotalSum));
		assertEquals(USELESS_NOTE, order.getNotes());
		assertItemDataJsonCreated(order);
		return order;
	}

	private void checkOutCartForPayFromReferralBalanceThrowsInsufficient(JSONObject requestBody, BigDecimal total, BigDecimal subTotal, BigDecimal shippingFee) {
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", request, Order.class);
		assertEquals(406, res.getStatusCodeValue());

	}
	private void assertItemDataJsonCreated(Order order) {
		Set<BasketItem> returnedItems = getBasketItemFromResponse(order);
		Set<BasketItem> savedItemsData = parseItemsDataJson(order);
		assertEquals(savedItemsData, returnedItems);
	}

	private Set<BasketItem> getBasketItemFromResponse(Order order) {
		return order
				.getSubOrders()
				.stream()
				.map(SubOrder::getItems)
				.flatMap(List::stream)
				.map(this::cloneBasketItem)
				.peek(clone -> clone.setThumb(null)) //save item data json don't include the thumbnail
				.peek(clone -> clone.setId(null)) //save item data json don't include the basket Item Id
				.collect(toSet());
	}

	private Set<BasketItem> parseItemsDataJson(Order order) {
		return order
				.getSubOrders()
				.stream()
				.map(SubOrder::getItems)
				.flatMap(List::stream)
				.map(BasketItem::getId)
				.collect(
						collectingAndThen(toList(), ids -> basketRepo.findByIdIn(ids, 99001L)))
				.stream()
				.map(BasketsEntity::getItemData)
				.map(this::parseAsBasketItem)
				.collect(toSet());
	}

	private BasketItem cloneBasketItem(BasketItem source){
		BasketItem target = new BasketItem();
		BeanUtils.copyProperties(source, target);
		return target;
	}

	private BasketItem parseAsBasketItem(String itemData){
		try {
			return objectMapper.readValue(itemData, BasketItem.class);
		} catch (IOException e) {
			e.printStackTrace();
			return new BasketItem();
		}
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
		JSONObject requestBody = createCartCheckoutBody();
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "101112");

		ResponseEntity<String> response = template.postForEntity("/cart/checkout", request, String.class);

		assertEquals(NOT_FOUND, response.getStatusCode());
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
		body.put("requestedPoints", "0");

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
		body.put("notes", USELESS_NOTE);
		body.put("requestedPoints", "0");

		return body;
	}

	// TODO: make this test work with a swtich flag, that either make it work on bosta
	//staging server + mail.nasnav.org mail server
	//or make it work on mock bosta server + mock mail service
	@Test
	@Ignore("this test make calls to bosta test server")
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_4.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void orderCompleteCycle() throws BusinessException {

		addCartItems(88L, 602L, 2);
		addCartItems(88L, 604L, 1);
		
		//checkout
		JSONObject requestBody = createCartCheckoutBodyForCompleteCycleTest();

		Order order = checkOutCart(requestBody, new BigDecimal("3125"), new BigDecimal("3100"), new BigDecimal("25"));
		Long orderId = order.getOrderId();
		
		HttpEntity<?> request = getHttpEntity("123");
		template.postForEntity("/payment/cod/execute?order_id="+orderId, request, Void.class);
//		orderService.finalizeOrder(orderId);
		
		asList(new ShopManager(502L, "161718"), new ShopManager(501L,"131415"))
		.forEach(mgr -> confrimOrder(order, mgr));
	}

// TODO: make this test work with a swtich flag, that either make it work on bosta
	//staging server + mail.nasnav.org mail server
	//or make it work on mock bosta server + mock mail service
//	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_4.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void orderRejectCompleteCycle() throws BusinessException {

		addCartItems(88L, 602L, 2);
		addCartItems(88L, 604L, 1);
		
		//checkout
		JSONObject requestBody = createCartCheckoutBodyForCompleteCycleTest();

		Order order = checkOutCart(requestBody, new BigDecimal("3125"), new BigDecimal("3100"), new BigDecimal("25"));
		Long orderId = order.getOrderId();
		
		orderService.finalizeOrder(orderId);
		
		asList(new ShopManager(502L, "161718"), new ShopManager(501L,"131415"))
		.forEach(mgr -> rejectOrder(order, mgr));
	}

	// TODO: make this test work with a swtich flag, that either make it work on bosta
	//staging server + mail.nasnav.org mail server
	//or make it work on mock bosta server + mock mail service
//		@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_4.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void orderCancelCompleteCycle() throws BusinessException {

		addCartItems(88L, 602L, 2);
		addCartItems(88L, 604L, 1);
		
		//checkout
		JSONObject requestBody = createCartCheckoutBodyForCompleteCycleTest();

		Order order = checkOutCart(requestBody, new BigDecimal("3125"), new BigDecimal("3100"), new BigDecimal("25"));
		Long orderId = order.getOrderId();
		
		orderService.finalizeOrder(orderId);
		
		HttpEntity<?> request = getHttpEntity("123");
		ResponseEntity<String> res = template.postForEntity("/order/cancel?meta_order_id="+orderId, request, String.class);
		assertEquals(OK, res.getStatusCode());
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
		Long userId = 88L;
		Cart initialCart = cartService.getUserCart(userId);
		//---------------------------------------------------------------		
		String requestBody = createCartCheckoutBody().toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res = 
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);
		
		//---------------------------------------------------------------
		Cart cart = res.getBody().getCart();
		List<Long> stockIdsAfter = 
				cart.getItems().stream().map(CartItem::getStockId).collect(toList());
		
		assertEquals(OK, res.getStatusCode());
		assertEquals(2, cart.getItems().size());
		assertTrue("The optimization should pick stocks from a shop in cairo that can provide most items"
					, asList(607L, 609L).stream().allMatch(stockIdsAfter::contains));
		assertTrue(res.getBody().getTotalChanged());		
		
		//---------------------------------------------------------------
		Cart cartAfter = cartService.getUserCart(userId);
		
		assertEquals(initialCart, cartAfter);
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_6.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartSameCityTestWithNullAddress() {
		Long userId = 88L;
		Cart initialCart = cartService.getUserCart(userId);
		//---------------------------------------------------------------
		String requestBody = createCartCheckoutBody().put("customer_address", JSONObject.NULL).toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res =
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);

		//---------------------------------------------------------------
		Cart cart = res.getBody().getCart();
		List<Long> stockIdsAfter =
				cart.getItems().stream().map(CartItem::getStockId).collect(toList());

		assertEquals(OK, res.getStatusCode());
		assertEquals(2, cart.getItems().size());
		assertTrue("The optimization should pick stocks from a shop in cairo that can provide most items"
				, asList(607L, 609L).stream().allMatch(stockIdsAfter::contains));
		assertTrue(res.getBody().getTotalChanged());

		//---------------------------------------------------------------
		Cart cartAfter = cartService.getUserCart(userId);

		assertEquals(initialCart, cartAfter);
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_7.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartSelectShopWithHighestStockTest() {
		
		//---------------------------------------------------------------		
		String requestBody = createCartCheckoutBody().toString();
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
		JSONObject requestJson = createCartCheckoutBody();
		requestJson.put("additional_data", json().put("SHOP_ID", 503L));
		
		String requestBody = requestJson.toString();
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
	public void optimizeCartWithReferralDiscount() {

		JSONObject requestJson = createCartCheckoutBody();
		requestJson.put("referralCode", "abcdfg");

		String requestBody = requestJson.toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res =
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);

		Cart cart = res.getBody().getCart();

		assertEquals(OK, res.getStatusCode());
		assertEquals(new BigDecimal("243.00"), cart.getDiscount());
		assertEquals(new BigDecimal("243.00"), cart.getItems()
				.stream()
				.map(CartItem::getDiscount)
				.reduce(ZERO, BigDecimal::add));
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_16.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartWithReferralDiscountNotAppliedDateOutOfRange() {

		JSONObject requestJson = createCartCheckoutBody();
		requestJson.put("referralCode", "abcdfg");

		String requestBody = requestJson.toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res =
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);

		Cart cart = res.getBody().getCart();

		assertEquals(OK, res.getStatusCode());
		assertEquals(ZERO, cart.getDiscount());
		assertEquals(new BigDecimal("0.00"), cart.getItems()
				.stream()
				.map(CartItem::getDiscount)
				.reduce(ZERO, BigDecimal::add));
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_7.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartWithPayByReferralWallet() {

		//---------------------------------------------------------------
		JSONObject requestJson = createCartCheckoutBody();
		requestJson.put("referralCode", "abcdfg");
		requestJson.put("payFromReferralBalance", true);

		String requestBody = requestJson.toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res =
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);

		//---------------------------------------------------------------
		Cart cart = res.getBody().getCart();

		assertEquals(OK, res.getStatusCode());
		assertEquals(new BigDecimal("8100.00"), cart.getDiscount());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Insufficient_Wallet.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartWithPayByReferralWalletNotApplied() {
		JSONObject requestJson = createCartCheckoutBody();
		requestJson.put("referralCode", "abcdfg");
		requestJson.put("payFromReferralBalance", true);

		String requestBody = requestJson.toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res =
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	}


	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_16.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartWithPayByReferralWalletWithDateIntervalRageOut() {

		JSONObject requestJson = createCartCheckoutBody();
		requestJson.put("referralCode", "abcdfg");
		requestJson.put("payFromReferralBalance", true);

		String requestBody = requestJson.toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res =
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);

		Cart cart = res.getBody().getCart();

		assertEquals(OK, res.getStatusCode());
		assertEquals(ZERO, cart.getDiscount());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_7.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartSelectShopThatHaveNoEnoughQuantity() {
		
		//---------------------------------------------------------------
		JSONObject requestJson = createCartCheckoutBody();
		requestJson.put("additional_data", json().put("SHOP_ID", 504L));
		
		String requestBody = requestJson.toString();
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
				+ "have enough quantity for first item, so , it will pick it from the shop next in priority "
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
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Optimize_Test_Data_5.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void optimizeCartOutOfStockExistInWishlistTest() {
		JSONObject requestJson =
				new JSONObject()
					.put("customer_address", 12300001)
					.put("shipping_service_id", "BOSTA_LEVIS");

		String requestBody = requestJson.toString();
		HttpEntity<?> request = getHttpEntity(requestBody, "123");
		ResponseEntity<CartOptimizeResponseDTO> res =
				template.postForEntity("/cart/optimize", request, CartOptimizeResponseDTO.class);

		Long wishlistCount = wishlistRepo.count();
		Long cartItemsCount = cartItemRepo.countByUser_Id(88L);
		wishlistRepo.findCurrentCartItemsByUser_Id(88L);

		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
		assertEquals(1, wishlistCount.longValue());
		assertEquals(0, cartItemsCount.longValue());
	}

	@Test
	public void optimizeCartNoAuthN() {
		JSONObject requestBody = createCartCheckoutBody();
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "101112");
		ResponseEntity<Cart> response =
        		template.exchange("/cart/optimize", POST, request, Cart.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
	}

	//TODO: invalid parameter json test
	





	private void assertOrdersStatusAfterCheckout(Long unpaidOrderId, Long cancelPaymentOrderId, Long paidOrderId, Long errorPaymentOrderId) {
		MetaOrderEntity unpaidOrderAfter = metaOrderRepo.findFullDataById(unpaidOrderId).get();
		MetaOrderEntity cancelPaymentOrderAfter = metaOrderRepo.findFullDataById(cancelPaymentOrderId).get();
		MetaOrderEntity paidOrderAfter = metaOrderRepo.findFullDataById(paidOrderId).get();
		MetaOrderEntity errorPaymentOrder = metaOrderRepo.findFullDataById(errorPaymentOrderId).get();
		
		assertEquals(DISCARDED.getValue(), unpaidOrderAfter.getStatus());
		assertEquals(DISCARDED.getValue(), cancelPaymentOrderAfter.getStatus());
		assertEquals(DISCARDED.getValue(), errorPaymentOrder.getStatus());
		assertEquals(STORE_CONFIRMED.getValue(), paidOrderAfter.getStatus());
		
		asList(unpaidOrderAfter, cancelPaymentOrderAfter, errorPaymentOrder)
		.stream()
		.map(MetaOrderEntity::getSubOrders)
		.flatMap(Set::stream)
		.forEach(subOrder -> assertEquals(DISCARDED.getValue(), subOrder.getStatus()));
		
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
		ResponseEntity<OrderConfirmResponseDTO> res =
				template.postForEntity("/order/confirm?order_id=" + subOrderId, request, OrderConfirmResponseDTO.class);
		
		String billFile = res.getBody().getShippingBill();
		OrdersEntity orderEntity = orderRepo.findByIdAndShopsEntity_Id(subOrderId, mgr.getShopId()).get();
		ShipmentEntity shipment = orderEntity.getShipment();
		
		assertFalse(billFile.isEmpty());
		assertNotNull(shipment.getTrackNumber());
		assertNotNull(shipment.getExternalId());
		assertEquals(STORE_CONFIRMED.getValue(), orderEntity.getStatus());
	}

	private void rejectOrder(Order order, ShopManager mgr) {
		Long subOrderId = getSubOrderIdOfShop(order, mgr.getShopId());
		HttpEntity<?> request = createOrderRejectRequest(subOrderId, "oops!", mgr.getManagerAuthToken()); 
		template.postForEntity("/order/reject" , request, String.class);
		
		OrdersEntity orderEntity = orderRepo.findByIdAndShopsEntity_Id(subOrderId, mgr.getShopId()).get();
		ShipmentEntity shipment = orderEntity.getShipment();
		
		assertNull(shipment.getTrackNumber());
		assertNull(shipment.getExternalId());
		assertEquals(STORE_CANCELLED.getValue(), orderEntity.getStatus());
	}

	private HttpEntity<?> createOrderRejectRequest(Long orderId, String rejectionReason, String authToken) {
		String requestBody = 
				json()
				.put("sub_order_id", orderId)
				.put("rejection_reason", rejectionReason)
				.toString();
		return getHttpEntity(requestBody, authToken);
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
		body.put("notes", "come after dinner");
		return body;
	}

	private JSONObject createCartCheckoutBodyForCompleteCycleTestForEmployeeUser() {
		JSONObject body = new JSONObject();
		Map<String, String> additionalData = new HashMap<>();
		body.put("customer_address", 12300001);
		body.put("shipping_service_id", SERVICE_ID);
		body.put("additional_data", additionalData);
		body.put("notes", "come after dinner");
		body.put("customerId",88L);
		body.put("requestedPoints", "0");
		body.put("otp", "123456");

		return body;
	}

	//@Test test ignored as auto optimization changes are not acceptable now
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_10.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutWithWareHouseOptimizationStrategy() {
		JSONObject requestBody = createCartCheckoutBody();
		Order order = checkOutCart(requestBody, new BigDecimal("8125.5"), new BigDecimal("8100") ,new BigDecimal("25.5"));
		
		List<BasketItem> items = 
				order
				.getSubOrders()
				.stream()
				.map(SubOrder::getItems)
				.flatMap(List::stream)
				.collect(toList());
		List<Long> orderStocks = 
				items
				.stream()
				.map(BasketItem::getStockId)
				.collect(toList());
		assertEquals(3, items.size());
		assertTrue("The optimization should pick stocks from warehouse only"
					, asList(613L, 614L, 615L).stream().allMatch(orderStocks::contains));	
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Store_Checkout.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void initiateCheckout() throws MessagingException, IOException {
		doNothing()
				.when(mailService).send(
						any(String.class), any(String.class), any(String.class), any(String.class), any(Map.class)
				);
		HttpEntity<?> request = getHttpEntity( "101112");
		ResponseEntity<String> res = template.postForEntity("/cart/store-checkout/initiate?user_id=" + 88, request, String.class);
		assertEquals(200, res.getStatusCodeValue());

		UserEntity userEntity = userRepository.findById(88L).get();
		UserOtpEntity userOtp = userOtpRepository.findByUser(userEntity).orElse(null);
		assertNotNull(userOtp);
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Store_Checkout.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void initiateCheckoutUserNotFound() {
		HttpEntity<?> request = getHttpEntity( "101112");
		ResponseEntity<String> res = template.postForEntity("/cart/store-checkout/initiate?user_id=" + 888, request, String.class);
		assertEquals(404, res.getStatusCodeValue());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Store_Checkout.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void initiateCheckoutMailNotSent() throws MessagingException, IOException {
		doThrow(RuntimeBusinessException.class)
				.when(mailService).send(
						any(String.class), any(String.class), any(String.class), any(String.class), any(Map.class)
				);
		HttpEntity<?> request = getHttpEntity( "101112");
		ResponseEntity<String> res = template.postForEntity("/cart/store-checkout/initiate?user_id=" + 88, request, String.class);
		assertEquals(500, res.getStatusCodeValue());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Store_Checkout_1.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void completeCheckout()  {
		JSONObject requestBody = createCartCheckoutBodyForCompleteCycleTestForEmployeeUser();
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "101112");

		ResponseEntity<Order> res = template.postForEntity("/cart/store-checkout/complete", request, Order.class);
		assertEquals(200, res.getStatusCodeValue());
	}

	@Test
	public void timeEstimateTest(){
		JSONObject requestBody =new JSONObject();
		requestBody.put("currency", "EGP");
		requestBody.put("amount", 500);
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");

		ResponseEntity<Order> res = template.postForEntity("/cart/estimate-tokens-to-usdc", request, Order.class);
		assertEquals(200, res.getStatusCodeValue());
	}



	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_Store_Checkout_1.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void completeCheckoutNotValidOtp()  {
		JSONObject requestBody = createCartCheckoutBodyForCompleteCycleTestForEmployeeUser();
		requestBody.put("otp", "654321");
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "101112");

		ResponseEntity<ErrorResponseDTO> res = template.postForEntity("/cart/store-checkout/complete", request, ErrorResponseDTO.class);
		assertEquals(400, res.getStatusCodeValue());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_10.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutWithWareHouseOptimizationStrategyWithMissingParameter() {
		clearWarehouseOptimizationParameters();
		
		JSONObject requestBody = createCartCheckoutBody();
		
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", request, Order.class);
		assertEquals(500, res.getStatusCodeValue());
	}


	private void clearWarehouseOptimizationParameters() {
		OrganizationCartOptimizationEntity entity = 
				orgOptimizerRepo
				.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(WAREHOUSE.getValue(), 99001L)
				.get();
		entity.setParameters("{}");
		orgOptimizerRepo.save(entity);
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_11.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void checkoutWithWareHouseOptimizationStrategyWithInsuffecientStock() {
		JSONObject requestBody = createCartCheckoutBody();
		
		HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", request, Order.class);
		assertEquals(406, res.getStatusCodeValue());
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_12.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void getCartRemovedProductAndVariant() {
		HttpEntity<?> request =  getHttpEntity("123");
		ResponseEntity<Cart> response =
				template.exchange("/cart", GET, request, Cart.class);

		assertEquals(OK, response.getStatusCode());
		assertEquals(2, response.getBody().getItems().size());
		assertProductNamesReturned(response);
	}

	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data_12.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void addCartItemRemovedProductAndVariant() {
		//stock with removed variant
		JSONObject item = createCartItem(603L, 1);
		HttpEntity<?> request =  getHttpEntity(item.toString(),"123");
		ResponseEntity<Cart> response = template.exchange("/cart/item", POST, request, Cart.class);
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());

		//stock with removed product
		item = createCartItem(605L, 1);
		request =  getHttpEntity(item.toString(),"123");
		response = template.exchange("/cart/item", POST, request, Cart.class);
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}



	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getV2CartNoAuthz() {
		HttpEntity<?> request = getHttpEntity("NOT FOUND");
		ResponseEntity<Cart> response =
				template.exchange("/cart/2", GET, request, Cart.class);

		Assert.assertEquals(UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	public void getV2CartNoToken() {
		ResponseEntity<Cart> response =
				template.getForEntity("/cart/v2", Cart.class);
		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}


	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getCartv2NoAuthN() {
		HttpEntity<?> request = getHttpEntity("101112");
		ResponseEntity<Cart> response =
				template.exchange("/cart/v2", GET, request, Cart.class);

		Assert.assertEquals(FORBIDDEN, response.getStatusCode());
	}


	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getCartSuccessV2() {
		HttpEntity<?> request = getHttpEntity("123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/v2", GET, request, Cart.class);

		Assert.assertEquals(OK, response.getStatusCode());
		Assert.assertEquals(2, response.getBody().getItems().size());
		assertProductNamesReturned(response);
	}





}



@Data
@AllArgsConstructor
class ShopManager{
	private Long shopId;
	private String managerAuthToken;
}
