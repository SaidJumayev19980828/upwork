package com.nasnav.yeshtery.test.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.*;
import com.nasnav.dto.BasketItem;
import com.nasnav.dto.response.navbox.*;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.OrderService;
import com.nasnav.yeshtery.Yeshtery;
import com.nasnav.commons.YeshteryConstants;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.enumerations.OrderStatus.*;
import static com.nasnav.service.helpers.CartServiceHelper.ADDITIONAL_DATA_PRODUCT_ID;
import static com.nasnav.service.helpers.CartServiceHelper.ADDITIONAL_DATA_PRODUCT_TYPE;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.SERVICE_ID;
import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.yeshtery.test.commons.TestCommons.json;
import static com.nasnav.yeshtery.test.controllers.YeshteryOrdersControllerTest.USELESS_NOTE;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})

public class YeshteryCartControllerTest {

    private final String YESHTERY_CART_API_PATH = YeshteryConstants.API_PATH + "/cart";
    private final String YESHTERY_CART_ITEM_API_PATH = YeshteryConstants.API_PATH + "/cart/item";
    private final String YESHTERY_CART_CHECKOUT_API_PATH = YeshteryConstants.API_PATH + "/cart/checkout";

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartItemRepository cartItemRepo;

    @Autowired
    private MetaOrderRepository metaOrderRepo;

    @Autowired
    private BasketRepository basketRepo;

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmployeeUserRepository empRepo;


    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getCartNoAuthz() {
        HttpEntity<?> request = getHttpEntity("NOT FOUND");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_API_PATH, GET, request, Cart.class);

        Assert.assertEquals(UNAUTHORIZED, response.getStatusCode());
    }

    @Test
	public void getCartNoToken() {
        ResponseEntity<Cart> response =
        		template.getForEntity("/cart", Cart.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
	}


    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getCartNoAuthN() {
        HttpEntity<?> request = getHttpEntity("101112");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_API_PATH, GET, request, Cart.class);

        Assert.assertEquals(FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getCartSuccess() {
        HttpEntity<?> request = getHttpEntity("123");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_API_PATH, GET, request, Cart.class);

        Assert.assertEquals(OK, response.getStatusCode());
        Assert.assertEquals(2, response.getBody().getItems().size());
        assertProductNamesReturned(response);
    }
    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getCartWithUserIdSuccess() {
        EmployeeUserEntity user = empRepo.findById(68L).get();
        String authtoken = user.getAuthenticationToken();

        HttpEntity<?> request = getHttpEntity(authtoken);
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_API_PATH +"/"+88L, GET, request, Cart.class);

        Assert.assertEquals(OK, response.getStatusCode());
        Assert.assertEquals(2, response.getBody().getItems().size());
        assertProductNamesReturned(response);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkRoleUserToGetCartWithUserIdSuccess() {
        UserEntity user = userRepository.findById(88L).get();
        String authtoken = user.getAuthenticationToken();

        HttpEntity<?> request = getHttpEntity(authtoken);
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_API_PATH +"/"+88L, GET, request, Cart.class);

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
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void addCartItemZeroQuantity() {
        Long itemsCountBefore = cartItemRepo.countByUser_Id(88L);

        JSONObject item = createCartItem();
        item.put("stock_id", 602);
        item.put("quantity", 0);

        HttpEntity<?> request = getHttpEntity(item.toString(), "123");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(itemsCountBefore - 1, response.getBody().getItems().size());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void addCartItemSuccess() {
        addCartItems(88L, 606L, 1);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void addCartItemWithAdditionalData() {
        var userId = 88L;
        var stockId = 606L;
        var quantity = 1;
        var collectionId = 1009;
        var productType = 2;
        Long itemsCountBefore = cartItemRepo.countByUser_Id(userId);

        JSONObject additionalData =
                json()
                        .put(ADDITIONAL_DATA_PRODUCT_ID, collectionId)
                        .put(ADDITIONAL_DATA_PRODUCT_TYPE, productType);
        JSONObject itemJson = createCartItem(stockId, quantity);
        itemJson.put("additional_data", additionalData);

        HttpEntity<?> request = getHttpEntity(itemJson.toString(), "123");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(itemsCountBefore + 1, response.getBody().getItems().size());

        CartItem item = getCartItemOfStock(stockId, response);

        Assert.assertEquals(additionalData.toMap().keySet(), item.getAdditionalData().keySet());
        Assert.assertEquals(new HashSet<>(additionalData.toMap().values()), new HashSet<>(item.getAdditionalData().values()));
        Assert.assertEquals(collectionId, item.getProductId().intValue());
        Assert.assertEquals(productType, item.getProductType().intValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void addCartItemWithAdditionalDataAddedToMainJson() {
        var userId = 88L;
        var stockId = 606L;
        var quantity = 1;
        var collectionId = 1009;
        var productType = 2;
        var itemsCountBefore = cartItemRepo.countByUser_Id(userId);

        JSONObject additionalData = json();
        JSONObject itemJson = createCartItem(stockId, quantity);
        itemJson
                .put("additional_data", additionalData)
                .put("product_id", collectionId)
                .put("product_type", productType);

        HttpEntity<?> request = getHttpEntity(itemJson.toString(), "123");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(itemsCountBefore + 1, response.getBody().getItems().size());

        CartItem item = getCartItemOfStock(stockId, response);

        Set<String> expectedAdditionalDataFields = setOf(ADDITIONAL_DATA_PRODUCT_ID, ADDITIONAL_DATA_PRODUCT_TYPE);
        Set<Integer> expectedAdditionalDataValues = setOf(collectionId, productType);
        Assert.assertEquals(expectedAdditionalDataFields, item.getAdditionalData().keySet());
        Assert.assertEquals(expectedAdditionalDataValues, new HashSet<>(item.getAdditionalData().values()));
        Assert.assertEquals(collectionId, item.getProductId().intValue());
        Assert.assertEquals(productType, item.getProductType().intValue());
    }

    private CartItem getCartItemOfStock(Long stockId, ResponseEntity<Cart> response) {
        return response
                .getBody()
                .getItems()
                .stream()
                .filter(it -> it.getStockId().equals(stockId))
                .findFirst()
                .get();
    }


    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void addCartItemNoStock() {
        JSONObject item = createCartItem();
        item.remove("stock_id");

        HttpEntity<?> request = getHttpEntity(item.toString(), "123");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void addCartItemNoQuantity() {
        JSONObject item = createCartItem();
        item.remove("quantity");

        HttpEntity<?> request = getHttpEntity(item.toString(), "123");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void addCartItemNegativeQuantity() {
        JSONObject item = createCartItem();
        item.put("quantity", -1);

        HttpEntity<?> request = getHttpEntity(item.toString(), "123");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    @Test
    public void addCartNoAuthz() {
        JSONObject item = createCartItem();
        HttpEntity<?> request = getHttpEntity(item.toString(), "NOT FOUND");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);

        Assert.assertEquals(UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void addCartNoAuthN() {
        JSONObject item = createCartItem();
        HttpEntity<?> request = getHttpEntity(item.toString(), "101112");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);

        Assert.assertEquals(FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void deleteCartItemSuccess() {
        var itemsCountBefore = cartItemRepo.countByUser_Id(88L);
        var itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
        HttpEntity<?> request = getHttpEntity("123");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH + "?item_id=" + itemId, DELETE, request, Cart.class);

        Assert.assertEquals(OK, response.getStatusCode());
        Assert.assertEquals(itemsCountBefore - 1, response.getBody().getItems().size());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void removeCartNoAuthz() {
        var itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
        HttpEntity<?> request = getHttpEntity("NOT FOUND");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH + "?item_id=" + itemId, DELETE, request, Cart.class);

        Assert.assertEquals(UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void removeCartNoAuthN() {
        var itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
        HttpEntity<?> request = getHttpEntity("101112");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH + "?item_id=" + itemId, DELETE, request, Cart.class);

        Assert.assertEquals(FORBIDDEN, response.getStatusCode());
    }

    private JSONObject createCartItem(Long stockId, Integer quantity) {
        JSONObject item = new JSONObject();
        item.put("stock_id", stockId);
        item.put("cover_img", "img");
        item.put("quantity", quantity);

        return item;
    }

    private JSONObject createCartItem() {
        return createCartItem(606L, 1);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartSuccess() {
        checkoutCart();
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartWithDiscounts() {
        JSONObject requestBody = createCartCheckoutBody();

        checkOutCart(requestBody, new BigDecimal("5891"), new BigDecimal("5840"), new BigDecimal("51"));
    }


    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartWithUsedPromotions() {
        JSONObject requestBody = createCartCheckoutBody();
        requestBody.put("promo_code", "GREEEEEED_HEARt");

        HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
        ResponseEntity<Order> res = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, Order.class);

        Assert.assertEquals("if promocode was already used, checkout will fail", 406, res.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartWithPromotionsWithDifferentCase() {
        JSONObject requestBody = createCartCheckoutBody();
        requestBody.put("promo_code", "gReEeEeED");

        Order order = checkOutCart(requestBody, new BigDecimal("5790.45"), new BigDecimal("5840"), new BigDecimal("51"));

        MetaOrderEntity entity = metaOrderRepo.findByMetaOrderId(order.getOrderId()).get();
        var promoId = entity.getPromotions().stream().findFirst().get().getId();

        Assert.assertEquals(630002L, promoId.longValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartWithPromotionsWithPercentage() {
        JSONObject requestBody = createCartCheckoutBody();
        requestBody.put("promo_code", "MORE_GREEEEEEED");

        Order order = checkOutCart(requestBody, new BigDecimal("5249.18"), new BigDecimal("5840"), new BigDecimal("51"));

        MetaOrderEntity entity = metaOrderRepo.findByMetaOrderId(order.getOrderId()).get();
        var promoId = entity.getPromotions().stream().findFirst().get().getId();

        Assert.assertEquals(630003L, promoId.longValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartWithPromotionsWithPercentageButBelowMinCartValue() {
        JSONObject requestBody = createCartCheckoutBody();
        requestBody.put("promo_code", "SCAM_GREEEEEEED");

        HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
        ResponseEntity<Order> res = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, Order.class);
        Assert.assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartWithPromotionsWithPercentageButWithTooHighDiscount() {
        JSONObject requestBody = createCartCheckoutBody();
        requestBody.put("promo_code", "kafa");

        Order order = checkOutCart(requestBody, new BigDecimal("5881"), new BigDecimal("5840"), new BigDecimal("51"));

        MetaOrderEntity entity = metaOrderRepo.findByMetaOrderId(order.getOrderId()).get();
        var promoId = entity.getPromotions().stream().findFirst().get().getId();

        Assert.assertEquals(630006L, promoId.longValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_8.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartWithFailedOptimization() {
        JSONObject requestBody = createCartCheckoutBody();
        HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
        ResponseEntity<Order> res = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, Order.class);

        Assert.assertEquals("failure in optimization due to different prices will return an error.", 406, res.getStatusCodeValue());
    }

    private Order checkoutCart() {
        JSONObject requestBody = createCartCheckoutBody();

        Order body = checkOutCart(requestBody, new BigDecimal("3151"), new BigDecimal("3100"), new BigDecimal("51"));

        return body;
    }

    private Order checkOutCart(JSONObject requestBody, BigDecimal total, BigDecimal subTotal, BigDecimal shippingFee) {
        HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
        ResponseEntity<Order> res = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, Order.class);
        Assert.assertEquals(200, res.getStatusCodeValue());

        Order order = res.getBody();
        BigDecimal subOrderSubtTotalSum = getSubOrderSubTotalSum(order);
        BigDecimal subOrderTotalSum = getSubOrderTotalSum(order);
        BigDecimal subOrderShippingSum = getSubOrderShippingSum(order);

        assertTrue(order.getOrderId() != null);
        Assert.assertEquals(0, shippingFee.compareTo(order.getShipping()));
        Assert.assertEquals(0, subTotal.compareTo(order.getSubtotal()));
        Assert.assertEquals(0, total.compareTo(order.getTotal()));
        Assert.assertEquals(0, order.getShipping().compareTo(subOrderShippingSum));
        Assert.assertEquals(0, order.getSubtotal().compareTo(subOrderSubtTotalSum));
        Assert.assertEquals(0, order.getTotal().compareTo(subOrderTotalSum));
        Assert.assertEquals(USELESS_NOTE, order.getNotes());
        assertItemDataJsonCreated(order);
        return order;
    }

    private void assertItemDataJsonCreated(Order order) {
        Set<BasketItem> returnedItems = getBasketItemFromResponse(order);
        Set<BasketItem> savedItemsData = parseItemsDataJson(order);
        Assert.assertEquals(savedItemsData, returnedItems);
    }

    private Set<BasketItem> parseItemsDataJson(Order order) {
        return order
                .getSubOrders()
                .stream()
                .map(SubOrder::getItems)
                .flatMap(List::stream)
                .map(BasketItem::getId)
                .collect(
                        collectingAndThen(toList(), ids -> basketRepo.findByIdIn(ids)))
                .stream()
                .map(BasketsEntity::getItemData)
                .map(this::parseAsBasketItem)
                .collect(toSet());
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

    private BasketItem cloneBasketItem(BasketItem source) {
        BasketItem target = new BasketItem();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    private BasketItem parseAsBasketItem(String itemData) {
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
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartNoAuthZ() {
        HttpEntity<?> request = getHttpEntity("not_found");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, String.class);

        Assert.assertEquals(UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartNoAuthN() {
        HttpEntity<?> request = getHttpEntity("{}", "101112");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, String.class);

        Assert.assertEquals(FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartNoAddressId() {
        JSONObject body = createCartCheckoutBody();
        body.put("customer_address", -1);

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, String.class);
        Assert.assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartInvalidAddressId() {
        JSONObject body = new JSONObject();
        body.put("shipping_service_id", "Bosta");

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, String.class);
        Assert.assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartDifferentCurrencies() {
        //first add item with different currency
        JSONObject item = createCartItem();
        item.put("stock_id", 606);
        HttpEntity<?> request = getHttpEntity(item.toString(), "123");
        ResponseEntity<Cart> response = template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);
        Assert.assertEquals(200, response.getStatusCodeValue());

        //then try to checkout cart
        JSONObject body = createCartCheckoutBody();

        request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> res = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, String.class);
        Assert.assertEquals(406, res.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_2.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartZeroStock() {
        JSONObject body = createCartCheckoutBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> res = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, String.class);
        Assert.assertEquals(406, res.getStatusCodeValue());
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

        return body;
    }


    private void addCartItems(Long userId, Long stockId, Integer quantity) {
        var itemsCountBefore = cartItemRepo.countByUser_Id(userId);

        JSONObject item = createCartItem(stockId, quantity);

        HttpEntity<?> request = getHttpEntity(item.toString(), "123");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(itemsCountBefore + 1, response.getBody().getItems().size());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_5.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutCartAndDeleteDanglingOrdersTest() {
        var unpaidOrderId = 310001L;
        var cancelPaymentOrderId = 310002L;
        var paidOrderId = 310003L;
        var errorPaymentOrderId = 310004L;

        assertOrdersStatusBeforeCheckout(unpaidOrderId, cancelPaymentOrderId, paidOrderId, errorPaymentOrderId);

        JSONObject requestBody = createCartCheckoutBody();
        checkOutCart(requestBody, new BigDecimal("3151"), new BigDecimal("3100"), new BigDecimal("51"));

        assertOrdersStatusAfterCheckout(unpaidOrderId, cancelPaymentOrderId, paidOrderId, errorPaymentOrderId);
    }

    private void assertOrdersStatusAfterCheckout(Long unpaidOrderId, Long cancelPaymentOrderId, Long paidOrderId, Long errorPaymentOrderId) {
        MetaOrderEntity unpaidOrderAfter = metaOrderRepo.findFullDataById(unpaidOrderId).get();
        MetaOrderEntity cancelPaymentOrderAfter = metaOrderRepo.findFullDataById(cancelPaymentOrderId).get();
        MetaOrderEntity paidOrderAfter = metaOrderRepo.findFullDataById(paidOrderId).get();
        MetaOrderEntity errorPaymentOrder = metaOrderRepo.findFullDataById(errorPaymentOrderId).get();

        Assert.assertEquals(DISCARDED.getValue(), unpaidOrderAfter.getStatus());
        Assert.assertEquals(DISCARDED.getValue(), cancelPaymentOrderAfter.getStatus());
        Assert.assertEquals(DISCARDED.getValue(), errorPaymentOrder.getStatus());
        Assert.assertEquals(STORE_CONFIRMED.getValue(), paidOrderAfter.getStatus());

        asList(unpaidOrderAfter, cancelPaymentOrderAfter, errorPaymentOrder)
                .stream()
                .map(MetaOrderEntity::getSubOrders)
                .flatMap(Set::stream)
                .forEach(subOrder -> Assert.assertEquals(DISCARDED.getValue(), subOrder.getStatus()));

        paidOrderAfter
                .getSubOrders()
                .stream()
                .forEach(subOrder -> Assert.assertEquals(STORE_CONFIRMED.getValue(), subOrder.getStatus()));
    }

    private void assertOrdersStatusBeforeCheckout(Long unpaidOrderId, Long cancelPaymentOrderId, Long paidOrderId, Long errorPaymentOrderId) {
        MetaOrderEntity unpaidOrder = metaOrderRepo.findFullDataById(unpaidOrderId).get();
        MetaOrderEntity cancelPaymentOrder = metaOrderRepo.findFullDataById(cancelPaymentOrderId).get();
        MetaOrderEntity paidOrder = metaOrderRepo.findFullDataById(paidOrderId).get();
        MetaOrderEntity errorPaymentOrder = metaOrderRepo.findFullDataById(errorPaymentOrderId).get();

        Assert.assertEquals(CLIENT_CONFIRMED.getValue(), unpaidOrder.getStatus());
        Assert.assertEquals(CLIENT_CONFIRMED.getValue(), cancelPaymentOrder.getStatus());
        Assert.assertEquals(CLIENT_CONFIRMED.getValue(), errorPaymentOrder.getStatus());
        Assert.assertEquals(STORE_CONFIRMED.getValue(), paidOrder.getStatus());

        asList(unpaidOrder, cancelPaymentOrder, errorPaymentOrder)
                .stream()
                .map(MetaOrderEntity::getSubOrders)
                .flatMap(Set::stream)
                .forEach(subOrder -> Assert.assertEquals(CLIENT_CONFIRMED.getValue(), subOrder.getStatus()));

        paidOrder
                .getSubOrders()
                .stream()
                .forEach(subOrder -> Assert.assertEquals(STORE_CONFIRMED.getValue(), subOrder.getStatus()));
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


    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_11.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void checkoutWithWareHouseOptimizationStrategyWithInsuffecientStock() {
        JSONObject requestBody = createCartCheckoutBody();

        HttpEntity<?> request = getHttpEntity(requestBody.toString(), "123");
        ResponseEntity<Order> res = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, Order.class);
        Assert.assertEquals(406, res.getStatusCodeValue());
    }


    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_12.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getCartRemovedProductAndVariant() {
        HttpEntity<?> request = getHttpEntity("123");
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_API_PATH, GET, request, Cart.class);

        Assert.assertEquals(OK, response.getStatusCode());
        Assert.assertEquals(2, response.getBody().getItems().size());
        assertProductNamesReturned(response);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_12.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void addCartItemRemovedProductAndVariant() {
        //stock with removed variant
        JSONObject item = createCartItem(603L, 1);
        HttpEntity<?> request = getHttpEntity(item.toString(), "123");
        ResponseEntity<Cart> response = template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);
        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());

        //stock with removed product
        item = createCartItem(605L, 1);
        request = getHttpEntity(item.toString(), "123");
        response = template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);
        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

}
