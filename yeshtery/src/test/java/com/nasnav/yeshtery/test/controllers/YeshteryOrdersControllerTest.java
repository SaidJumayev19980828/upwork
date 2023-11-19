package com.nasnav.yeshtery.test.controllers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.shipping.ShipmentDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.dto.response.CategoryDto;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.dto.response.ReturnRequestDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.dto.response.navbox.Shipment;
import com.nasnav.dto.response.navbox.SubOrder;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.enumerations.ReturnRequestStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.OrdersListResponse;
import com.nasnav.response.ReturnRequestsResponse;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.AdminService;
import com.nasnav.service.OrderService;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.ShippingServiceFactory;
import com.nasnav.shipping.model.*;
import com.nasnav.shipping.services.FixedFeeStrictSameCityShippingService;
import com.nasnav.yeshtery.Yeshtery;
import com.nasnav.commons.YeshteryConstants;
import com.nasnav.yeshtery.controller.v1.YeshteryUserController;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.EntityUtils.DEFAULT_TIMESTAMP_PATTERN;
import static com.nasnav.enumerations.OrderStatus.*;
import static com.nasnav.enumerations.ReturnRequestStatus.*;
import static com.nasnav.enumerations.UserStatus.ACTIVATED;
import static com.nasnav.shipping.services.FixedFeeShippingService.*;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.SERVICE_ID;
import static com.nasnav.yeshtery.test.commons.TestCommons.*;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
@Slf4j
public class YeshteryOrdersControllerTest extends AbstractTestWithTempBaseDir {
    private final String PRODUCT_FEATURE_1_NAME = "Lispstick Color";
    private final String PRODUCT_FEATURE_1_P_NAME = "lipstick_color";
    private final String PRODUCT_FEATURE_2_NAME = "Lipstick flavour";
    private final String PRODUCT_FEATURE_2_P_NAME = "lipstick_flavour";

    private final String YESHTERY_CART_CHECKOUT_API_PATH = YeshteryConstants.API_PATH + "/cart/checkout";
    private final String YESHTERY_CART_ITEM_API_PATH = YeshteryConstants.API_PATH + "/cart/item";

    private final String YESHTERY_ORDER_CONFIRM_API_PATH = YeshteryConstants.API_PATH + "/order/confirm";
    private final String YESHTERY_ORDER_REJECT_API_PATH = YeshteryConstants.API_PATH + "/order/reject";
    private final String YESHTERY_ORDER_CANCEL_API_PATH = YeshteryConstants.API_PATH + "/order/cancel";
    private final String YESHTERY_ORDER_LIST_API_PATH = YeshteryConstants.API_PATH + "/order/list";
    private final String YESHTERY_ORDER_INFO_API_PATH = YeshteryConstants.API_PATH + "/order/info";
    private final String YESHTERY_ORDER_RETURN_API_PATH = YeshteryConstants.API_PATH + "/order/return";
    private final String YESHTERY_ORDER_RETURN_REQUESTS_API_PATH = YeshteryConstants.API_PATH + "/order/return/requests";
    private final String YESHTERY_ORDER_RETURN_REQUEST_API_PATH = YeshteryConstants.API_PATH + "/order/return/request";
    private final String YESHTERY_ORDER_RETURN_REJECT_API_PATH = YeshteryConstants.API_PATH + "/order/return/reject";
    private final String YESHTERY_ORDER_RETURN_CONFIRM_API_PATH = YeshteryConstants.API_PATH + "/order/return/confirm";
    private final String YESHTERY_ORDER_STATUS_UPDATE_API_PATH = YeshteryConstants.API_PATH + "/order/status/update";
    private final String YESHTERY_SHIPPING_OFFERS_API_PATH = YeshteryConstants.API_PATH + "/shipping/offers";
    private final String YESHTERY_GET_BRANDS_API_PATH = YeshteryConstants.API_PATH + "/yeshtery/brands";

    public static final String USELESS_NOTE = "come after dinner";
    private static final String EXPECTED_COVER_IMG_URL = "99001/img1.jpg";
    private static final Integer ETA_FROM = 1;
    private static final Integer ETA_TO = 2;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ObjectMapper mapper;


    // Repositories

    @Autowired
    private CartItemRepository cartItemRepo;
    @Autowired
    private LoyaltyTierRepository tierRepo;
    @Autowired
    private MetaOrderRepository metaOrderRepo;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private OrdersRepository orderRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTokenRepository tokenRepository;
    @Autowired
    private EmployeeUserRepository empRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private OrderService orderService;

    // Helpers & Services
    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReturnRequestRepository returnRequestRepo;
    @Autowired
    private LoyaltyPointTransactionRepository loyaltyPointTransactionRepo;
    @Autowired
    private LoyaltySpendTransactionRepository loyaltySpendTransactionRepo;
    @Autowired
    private ShippingServiceFactory shippingServiceFactory;

    @Mock
    private YeshteryUserController userController;

    @Autowired
    private AppConfig config;

    private MockMvc mockMvc;
    private UserEntity persistentUser;
    private OrganizationEntity organization;

    @Test
    public void getProductWithMultipleVariantsTest() {
        var response = template.getForEntity("/v1/yeshtery/product?product_id=1001", String.class);

        var expectedVariantFeatures = createExpectedFeaturesJson();
        var product = new JSONObject(response.getBody());
        var variantFeatures = product.getJSONArray("variant_features");
        var variants = product.getJSONArray("variants");

        assertEquals("Product 1001 has 5 variants, only the 4 with stock records will be returned", 4, variants.length());
        assertEquals("The product have only 2 variant features", 2, variantFeatures.length());
        JSONAssert.assertEquals(expectedVariantFeatures, variantFeatures, false);
    }


    private JSONArray createExpectedFeaturesJson() {
        var expectedFeature1 = new JSONObject();
        expectedFeature1.put("name", PRODUCT_FEATURE_1_NAME);
        expectedFeature1.put("label", PRODUCT_FEATURE_1_P_NAME);
        expectedFeature1.put("type", ProductFeatureType.STRING.name());
        expectedFeature1.put("extra_data", new JSONObject());

        var expectedFeature2 = new JSONObject();
        expectedFeature2.put("name", PRODUCT_FEATURE_2_NAME);
        expectedFeature2.put("label", PRODUCT_FEATURE_2_P_NAME);
        expectedFeature2.put("type", ProductFeatureType.STRING.name());
        expectedFeature2.put("extra_data", new JSONObject());

        return new JSONArray(Arrays.asList(expectedFeature1, expectedFeature2));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getCollectionTest() {
        var response = template.getForEntity("/v1/yeshtery/collection?id=1004", ProductDetailsDTO.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1004, response.getBody().getId().intValue());
    }

    @Test
    public void getYeshteryProductsTest() {
        var response = template.getForEntity("/v1/yeshtery/products", ProductsResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        List<ProductRepresentationObject> products = response.getBody().getProducts();
        assertEquals(3, products.size());
        assertEquals(1003, products.get(0).getId().intValue());
        assertEquals(1001, products.get(1).getId().intValue());
        assertEquals(1004, products.get(2).getId().intValue());
    }


    private void hasCoverImage(CategoryDto category) {
        assertTrue(category.getCover() != null);
        assertTrue(category.getLogo() != null);
    }


    @Test
    public void getTags() {

        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ParameterizedTypeReference<RestResponsePage<TagsRepresentationObject>> responseType = new ParameterizedTypeReference<>() {
        };
        Integer start = 0;
        Integer count = 10;
        ResponseEntity<RestResponsePage<TagsRepresentationObject>> response = template.exchange("/v1/yeshtery/tags?org_id=99001&start=" + start + "&count=" + count, HttpMethod.GET, httpEntity, responseType);

        Assert.assertEquals(200, response.getStatusCodeValue());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void getTagsTree() {
        ResponseEntity<List> response = template.getForEntity("/v1/yeshtery/tagstree?org_id=99001", List.class);
        List treeRoot = response.getBody();
        assertTrue(!treeRoot.isEmpty());
        Assert.assertEquals(1, treeRoot.size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Category_Test_Data_Insert_4.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getTagsTreeWithNodeHavingMultipleParents() throws JsonParseException, JsonMappingException, IOException {
        ResponseEntity<String> response = template.getForEntity("/v1/yeshtery/tagstree?org_id=99001", String.class);
        String body = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<TagsTreeNodeDTO>> typeReference = new TypeReference<List<TagsTreeNodeDTO>>() {
        };
        List<TagsTreeNodeDTO> tree = mapper.readValue(body, typeReference);
        long nodesNum =
                tree
                        .stream()
                        .filter(rootNode -> hasNodeWithId(rootNode, 50016L))
                        .count();

        assertTrue(!tree.isEmpty());
        Assert.assertEquals(3, tree.size());
        Assert.assertEquals("The node with multiple parents should appear as child multiple times", 2L, nodesNum);
    }

    private boolean hasNodeWithId(TagsTreeNodeDTO node, Long id) {
        return node
                .children
                .stream()
                .anyMatch(child -> Objects.equals(child.getNodeId(), id));
    }


    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void ordersListNasnavAdminDifferentFiltersTest() {

       String token = "101112";

        // no filters
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("details_level=3", token);

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("all orders ", 16, countOrdersFromResponse(response));

        //---------------------------------------------------------------------
        // by org_id
        response = sendOrdersListRequestWithParamsAndToken("org_id=99001&details_level=3", token);

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("8 orders with org_id = 99001", 8, countOrdersFromResponse(response));

        //---------------------------------------------------------------------
        // by shop_id
        response = sendOrdersListRequestWithParamsAndToken("shop_id=501&details_level=3", token);

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("3 orders with shop_id = 501", 3, countOrdersFromResponse(response));

        //---------------------------------------------------------------------
        // by user_id
        response = sendOrdersListRequestWithParamsAndToken("user_id=88&details_level=3", token);

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("6 orders with user_id = 88", 6, countOrdersFromResponse(response));

        //---------------------------------------------------------------------
        // by status
        response = sendOrdersListRequestWithParamsAndToken("status=NEW&details_level=3", token);

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("4 orders with status = NEW", 4, countOrdersFromResponse(response));

        //---------------------------------------------------------------------
        // by org_id and status
        response = sendOrdersListRequestWithParamsAndToken("org_id=99001&status=NEW&details_level=3", token);

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("1 orders with org_id = 99001 and status = NEW", 1, countOrdersFromResponse(response));

        //---------------------------------------------------------------------
        // by org_id and shop_id
        response = sendOrdersListRequestWithParamsAndToken("org_id=99001&shop_id=503&details_level=3", token);

        //---------------------------------------------------------------------
        // by org_id and user_id
        response = sendOrdersListRequestWithParamsAndToken("org_id=99002&user_id=90&details_level=3", token);


        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("1 order with org_id = 99002 and user_id = 90", 1, countOrdersFromResponse(response));

        //---------------------------------------------------------------------
        // by shop_id and status
        response = sendOrdersListRequestWithParamsAndToken("shop_id=501&status=NEW&details_level=3", token);

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("1 orders with shop_id = 501 and status = NEW", 1, countOrdersFromResponse(response));


        //---------------------------------------------------------------------
        // by user_id, shop_id and status
        response = sendOrdersListRequestWithParamsAndToken("user_id=88&shop_id=501&status=STORE_PREPARED&details_level=3", token);

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("1 order with user_id = 88 and shop_id = 501 and status = NEW", 1, countOrdersFromResponse(response));
    }

    @Test // Organization roles diffterent filters test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void ordersListOrganizationDifferentFiltersTest() {
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("details_level=3", "161718");

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("user#70 is Organization employee in org#99001 so he can view all orderes within org#99001", 8, countOrdersFromResponse(response));
        //-------------------------------------------------------------------------
        response = sendOrdersListRequestWithParamsAndToken("details_level=3", "131415");


        long org99002Orders = orderRepository.countByOrganizationEntity_id(99002L);
        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("user#69 is Organization admin in org#99002 so he can view all orderes within org#99002", org99002Orders, countOrdersFromResponse(response));

        //-------------------------------------------------------------------------
        response = sendOrdersListRequestWithParamsAndToken("details_level=3", "192021");

        var shopEmpId = 71L;
        var shopId = empRepository.findById(shopEmpId)
                .map(EmployeeUserEntity::getShopId)
                .get();
        long shopOrdersCount = orderRepository.countByShopsEntity_id(shopId);
        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals(format("user#%d is store employee in store#%d so he can view all orderes within the store", shopEmpId, shopId)
                , shopOrdersCount
                , countOrdersFromResponse(response));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void ordersListUnAuthTest() {
        // invalid user-id test
        ResponseEntity<String> response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?shop_id=501", GET,
                new HttpEntity<>(getHeaders("NO_EXISATING_TOKEN")), String.class); //no user with id = 99

        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void ordersListInvalidfiltersTest() {
        String token = "101112";
        // by shop_id only
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("shop_id=550", token);

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("No orders with shop_id = 550 ", 0, countOrdersFromResponse(response));

        // by user_id
        response = sendOrdersListRequestWithParamsAndToken("user_id=99", token);

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("no orders with user_id = 99", 0, countOrdersFromResponse(response));

        // by org_id
        response = sendOrdersListRequestWithParamsAndToken("org_id=999999", token);

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("no orders with org_id = 999999", 0, countOrdersFromResponse(response));

        // by status
        response = sendOrdersListRequestWithParamsAndToken("status=invalid_status", token);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void testDateFilteration() throws JsonProcessingException {
        modifyOrderUpdateTime(330044L, LocalDateTime.of(2017, 11, 26, 10, 00, 00));
        modifyOrderUpdateTime(330045L, LocalDateTime.of(2017, 12, 15, 10, 00, 00));
        modifyOrderUpdateTime(330046L, LocalDateTime.of(2017, 12, 16, 10, 00, 00));

        //-------------------------------------------------------------------
        // by shop_id only
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("updated_before=2017-12-23:12:12:12&updated_after=2017-12-01:12:12:12", "101112");

        assertEquals(OK, response.getStatusCode());
        assertEquals("expected 2 orders to be within this given time range ", 2, countOrdersFromResponse(response));
    }

    private void modifyOrderUpdateTime(Long orderId, LocalDateTime newUpdateTime) {
        jdbc.update("update orders set updated_at = ? where id = ?", newUpdateTime, orderId);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void testOrdersConsistency() {
        List<OrdersEntity> ordersList = orderRepository.findAll();

        for (OrdersEntity order : ordersList) {
            assertTrue(order.getUserId() != null);
        }
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Order_Info_Test.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListLevelTwoTest() throws IOException {

        ResponseEntity<OrdersListResponse> response =  sendOrdersListRequestWithParamsAndToken("details_level=2&count=1", "101112");
        List<DetailedOrderRepObject> orders = response.getBody().getOrders();
        DetailedOrderRepObject firstOrder = orders.get(0);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(firstOrder.getTotalQuantity() != null);
        assertTrue(firstOrder.getTotalQuantity() == 3);
        Assert.assertEquals(null, firstOrder.getItems());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Order_Info_Test.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListCountTest() throws IOException {
        String token = "101112";

        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("count=1", token);
        Assert.assertEquals(1, countOrdersFromResponse(response));

        response = sendOrdersListRequestWithParamsAndToken("count=2", token);
        Assert.assertEquals(2, countOrdersFromResponse(response));

        response = sendOrdersListRequestWithParamsAndToken("count=4", token);
        Assert.assertEquals(4, countOrdersFromResponse(response));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Order_Info_Test.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListStartTest() throws IOException {
        //count=1&
        String token = "101112";
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("start=1&count=1&details_level=3", token);
        List<DetailedOrderRepObject> orders = response.getBody().getOrders();
        DetailedOrderRepObject actualBody = orders.get(0);
        DetailedOrderRepObject expectedBody = createExpectedOrderInfo(330005L, new BigDecimal("50.00"), 1, "NEW", 89L, ZERO);

        Assert.assertEquals(expectedBody, actualBody);

        response = sendOrdersListRequestWithParamsAndToken("start=2&count=1&details_level=3", token);
        orders = response.getBody().getOrders();
        actualBody = orders.get(0);
        expectedBody = createExpectedOrderInfo(330004L, new BigDecimal("200.00"), 5, "NEW", 89L, new BigDecimal("100.00"));
        Assert.assertEquals(expectedBody, actualBody);

        response = sendOrdersListRequestWithParamsAndToken("start=3&count=1&details_level=3", token);
        orders = response.getBody().getOrders();
        actualBody = orders.get(0);
        expectedBody = createExpectedOrderInfo(330003L, new BigDecimal("300.00"), 7, "NEW", 88L, ZERO);
        Assert.assertEquals(expectedBody, actualBody);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Order_Info_Test.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListOrderByQuantityTest() throws IOException {
        String token = "101112";
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("details_level=3&orders_sorting_option=QUANTITY", token);
        List<DetailedOrderRepObject> orders = response.getBody().getOrders();

        Assert.assertEquals(330002L, orders.get(0).getOrderId());
        Assert.assertEquals(330005L, orders.get(orders.size() - 1).getOrderId());
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
                .readValue(response.getBody(), new TypeReference<List<DetailedOrderRepObject>>() {
                });
    }

    private DetailedOrderRepObject createExpectedOrderInfo(Long orderId, BigDecimal price, Integer quantity
            , String status, Long userId, BigDecimal discount) {
        OrdersEntity entity = getOrderEntityFullData(orderId);

        DetailedOrderRepObject order = new DetailedOrderRepObject();
        order.setUserId(userId);
        order.setUserName(entity.getName());
        order.setShopName(entity.getShopsEntity().getName());
        order.setCurrency("EGP");
        order.setCreatedAt(entity.getCreationDate());
        order.setDeliveryDate(entity.getDeliveryDate());
        order.setOrderId(orderId);
        order.setNotes("");
        order.setShippingAddress(null);
        order.setShopId(entity.getShopsEntity().getId());
        order.setStatus(status);
        order.setSubtotal(price);
        order.setTotal(price);
        order.setItems(createExpectedItems(price, quantity, discount));
        order.setTotalQuantity(quantity);
        order.setPaymentStatus(entity.getPaymentStatus().toString());
        order.setMetaOrderId(310001L);
        order.setDiscount(ZERO);
        order.setPoints(new ArrayList<>());
        order.setPaymentOperator("S.C.A.M");
        return order;
    }

    @Transactional
    public OrdersEntity getOrderEntityFullData(Long orderId) {
        return orderRepository.findFullDataById(orderId).get();
    }

    private List<BasketItem> createExpectedItems(BigDecimal price, Integer quantity, BigDecimal discount) {
        var discountAmount = discount.divide(new BigDecimal("100")).multiply(price).setScale(0);
        BasketItem item = new BasketItem();
        item.setProductId(1001L);
        item.setName("product_1");
        item.setStockId(601L);
        item.setQuantity(quantity);
        item.setTotalPrice(price.subtract(discountAmount).multiply(new BigDecimal(quantity)));
        item.setThumb(EXPECTED_COVER_IMG_URL);
        item.setPrice(price);
        return Arrays.asList(item);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_2.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListWithShipmentServiceFilter() throws IOException {
        HttpEntity request = getHttpEntity("101112");

        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("org_id=99001&shipping_service_id=BOSTA_LEVIS", "101112");
        List<DetailedOrderRepObject> ordersList = response.getBody().getOrders();

        assertEquals(OK, response.getStatusCode());
        checkOrdersHaveExpectedShippingService(ordersList);
    }

    private void checkOrdersHaveExpectedShippingService(List<DetailedOrderRepObject> resultOrdersList){
        List<Long> expectedOrdersIds = asList(330033L, 330037L, 330041L);

        resultOrdersList.forEach(ord -> assertTrue(expectedOrdersIds.contains(ord.getOrderId())));
        assertEquals(expectedOrdersIds.size(), resultOrdersList.size());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_2.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListWithPaymentOperatorFilter() throws IOException {
        HttpEntity request = getHttpEntity("101112");

        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("org_id=99001&payment_operator=COD", "101112");
        List<DetailedOrderRepObject> ordersList = response.getBody().getOrders();

        assertEquals(OK, response.getStatusCode());
        checkOrdersHaveExpectedPaymentOperator(ordersList);
    }

    private void checkOrdersHaveExpectedPaymentOperator(List<DetailedOrderRepObject> resultOrdersList){
        List<Long> expectedOrdersIds = asList(330033L, 330037L, 330042L, 330045L);

        resultOrdersList.forEach(ord -> assertTrue(expectedOrdersIds.contains(ord.getOrderId())));
        assertEquals(expectedOrdersIds.size(), resultOrdersList.size());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_2.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListWithPaymentAndShippingFilters() throws IOException {
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("org_id=99001&payment_operator=COD&shipping_service_id=BOSTA_LEVIS", "101112");

        List<DetailedOrderRepObject> ordersList = response.getBody().getOrders();

        assertEquals(OK, response.getStatusCode());
        checkOrdersHaveExpectedPaymentAndShipping(ordersList);
    }

    private void checkOrdersHaveExpectedPaymentAndShipping(List<DetailedOrderRepObject> resultOrdersList){
        List<Long> expectedOrdersIds = asList(330033L, 330037L);

        resultOrdersList.forEach(ord -> assertTrue(expectedOrdersIds.contains(ord.getOrderId())));
        assertEquals(expectedOrdersIds.size(), resultOrdersList.size());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_2.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListTotalTest(){
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("start=0&count=2", "252627");
        Long totalOrders = response.getBody().getTotal();

        assertEquals(9L, totalOrders.longValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_2.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListWithMinAndMaxFiltersTest(){
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("min_total=100&max_total=1000", "252627");
        Long totalOrders = response.getBody().getTotal();

        assertEquals(4L, totalOrders.longValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_2.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListSortingByTotalTest(){
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("orders_sorting_option=TOTAL", "252627");
        DetailedOrderRepObject higherTotalOrder = response.getBody().getOrders().get(0);

        assertEquals(330040L, higherTotalOrder.getOrderId());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_2.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListSortingByIdTest(){
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("orders_sorting_option=ID", "252627");
        DetailedOrderRepObject largestIdOrder = response.getBody().getOrders().get(0);

        assertEquals(330049L, largestIdOrder.getOrderId());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_2.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListSortingByCreationDateTest(){
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("orders_sorting_option=CREATION_DATE", "252627");
        DetailedOrderRepObject newestOrder = response.getBody().getOrders().get(0);

        assertEquals(330042L, newestOrder.getOrderId());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_2.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListFilterByCreationDateTest(){
        ResponseEntity<OrdersListResponse> response = sendOrdersListRequestWithParamsAndToken("created_after=2022-05-01:12:10:10&created_before=2022-05-30:12:10:10", "252627");

        assertEquals(3, countOrdersFromResponse(response));
    }

    private ResponseEntity<OrdersListResponse> sendOrdersListRequestWithParamsAndToken(String params, String token){
        HttpEntity<?> httpEntity = getHttpEntity(token);

        return template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?" + params,
                GET,
                httpEntity,
                OrdersListResponse.class);
    }

    private int countOrdersFromResponse(ResponseEntity<OrdersListResponse> response){
        return response.getBody().getOrders().size();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getAnyOrderYeshteryAdminTest() {
        ResponseEntity<DetailedOrderRepObject> response = template.exchange(YESHTERY_ORDER_INFO_API_PATH + "?order_id=330048", GET,
                getHttpEntity("101112"), DetailedOrderRepObject.class);
        Assert.assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() != null);

        response = template.exchange(YESHTERY_ORDER_INFO_API_PATH + "?order_id=330038", GET,
                getHttpEntity("101112"), DetailedOrderRepObject.class);
        Assert.assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() != null);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getReturnRequests() throws IOException {
        HttpEntity<?> request = getHttpEntity("131415");
        ResponseEntity<ReturnRequestsResponse> response = template.exchange(YESHTERY_ORDER_RETURN_REQUESTS_API_PATH, GET, request, ReturnRequestsResponse.class);
        Set<ReturnRequestDTO> body = response.getBody().getReturnRequests();
        Set<Long> ids = getReturnedRequestsIds(body);
        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(4, body.size());
        assertTrue(asList(330032L, 330031L, 440034L, 330036L).containsAll(ids));
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getReturnRequestsDifferentFilters() throws IOException {
        //count filter
        Set<ReturnRequestDTO> body = getReturnRequests("131415", "count=1");
        Assert.assertEquals(1, body.size());
        Set<Long> ids = getReturnedRequestsIds(body);
        assertTrue(setOf(440034L).containsAll(ids));

        //status filter
        body = getReturnRequests("131415", "status=NEW");
        Assert.assertEquals(2, body.size());
        ids = getReturnedRequestsIds(body);
        assertTrue(setOf(330031L, 330036L).containsAll(ids));

        //meta order filter
        body = getReturnRequests("131415", "meta_order_id=310001");
        Assert.assertEquals(1, body.size());
        ids = getReturnedRequestsIds(body);
        assertTrue(setOf(330031L).containsAll(ids));

        //shop id filter
        body = getReturnRequests("131415", "shop_id=501");
        Assert.assertEquals(1, body.size());
        ids = getReturnedRequestsIds(body);
        assertTrue(setOf(330031L).containsAll(ids));

        //date to filter
        String dateToStr =
                DateTimeFormatter
                        .ofPattern(DEFAULT_TIMESTAMP_PATTERN)
                        .format(now().minusDays(50L));
        body = getReturnRequests("131415", "date_to=" + dateToStr);
        Assert.assertEquals(1, body.size());
        ids = getReturnedRequestsIds(body);
        assertTrue(setOf(330032L).containsAll(ids));

        //date from filter
        String dateFromStr =
                DateTimeFormatter
                        .ofPattern(DEFAULT_TIMESTAMP_PATTERN)
                        .format(now().minusDays(50L));
        body = getReturnRequests("131415", "date_from=" + dateFromStr);
        Assert.assertEquals(3, body.size());
        ids = getReturnedRequestsIds(body);
        assertTrue(setOf(330031L, 440034L, 330036L).containsAll(ids));
    }

    private Set<ReturnRequestDTO> getReturnRequests(String authToken, String params) throws IOException {
        HttpEntity<?> request = getHttpEntity(authToken);
        ResponseEntity<ReturnRequestsResponse> response = template.exchange(YESHTERY_ORDER_RETURN_REQUESTS_API_PATH + "?" + params, GET, request, ReturnRequestsResponse.class);
        Assert.assertEquals(200, response.getStatusCodeValue());
        return response.getBody().getReturnRequests();
    }

    private Set<Long> getReturnedRequestsIds(Set<ReturnRequestDTO> returnedItems) {
        return returnedItems
                .stream()
                .map(ReturnRequestDTO::getId)
                .collect(toSet());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getReturnRequestsInvalidAuthorization() {
        HttpEntity<?> request = getHttpEntity("101112");
        ResponseEntity<String> response = template.exchange(YESHTERY_ORDER_RETURN_REQUESTS_API_PATH, GET, request, String.class);
        Assert.assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getReturnRequestsInvalidAuthentication() {
        HttpEntity<?> request = getHttpEntity("invalid token");
        ResponseEntity<String> response = template.exchange(YESHTERY_ORDER_RETURN_REQUESTS_API_PATH, GET, request, String.class);
        Assert.assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getReturnRequest() {
        HttpEntity<?> request = getHttpEntity("131415");
        ResponseEntity<ReturnRequestDTO> response = template.exchange(YESHTERY_ORDER_RETURN_REQUEST_API_PATH + "?id=330031", GET, request, ReturnRequestDTO.class);
        ReturnRequestDTO body = response.getBody();

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(330031, body.getId().longValue());
        Assert.assertEquals(310001, body.getMetaOrderId().longValue());
        assertFalse(body.getReturnedItems().isEmpty());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getReturnRequestAnotherOrg() {
        HttpEntity<?> request = getHttpEntity("131415");
        ResponseEntity<ReturnRequestDTO> response = template.exchange(YESHTERY_ORDER_RETURN_REQUEST_API_PATH + "?id=330033", GET, request, ReturnRequestDTO.class);

        Assert.assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getReturnRequestInvalidAuthorization() {
        HttpEntity<?> request = getHttpEntity("192021");
        ResponseEntity<ReturnRequestDTO> response = template.exchange(YESHTERY_ORDER_RETURN_REQUEST_API_PATH + "?id=330031", GET, request, ReturnRequestDTO.class);
        Assert.assertEquals(FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getReturnRequestInvalidAuthentication() {
        HttpEntity<?> request = getHttpEntity("invalid token");
        ResponseEntity<String> response = template.exchange(YESHTERY_ORDER_RETURN_REQUESTS_API_PATH + "?id=330031", GET, request, String.class);
        Assert.assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void customerCreateReturnRequestNoAuthZTest() {
        JSONObject body = createReturnRequestBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "101112");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_ORDER_RETURN_API_PATH, request, String.class);

        Assert.assertEquals(FORBIDDEN, response.getStatusCode());
    }

    private JSONObject createReturnRequestBody() {
        JSONArray returnedItems =
                jsonArray()
                        .put(json()
                                .put("order_item_id", 330037)
                                .put("returned_quantity", 1))
                        .put(json()
                                .put("order_item_id", 330038)
                                .put("returned_quantity", 2));
        return json().put("item_list", returnedItems);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void customerCreateReturnRequestNoAuthNTest() {
        JSONObject body = createReturnRequestBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "INVALID");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_ORDER_RETURN_API_PATH, request, String.class);

        Assert.assertEquals(UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void customerCreateReturnRequestOrderPast14DaysTest() {
        JSONObject body = createReturnRequestWithTooOldItemsBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_ORDER_RETURN_API_PATH, request, String.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    private JSONObject createReturnRequestWithTooOldItemsBody() {
        JSONArray returnedItems =
                jsonArray()
                        .put(json()
                                .put("order_item_id", 330035)
                                .put("returned_quantity", 1));
        return json().put("item_list", returnedItems);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void customerCreateReturnRequestOrderFromFinalizedOrderTest() {
        JSONObject body = createReturnRequestFromAFinalizedOrderBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_ORDER_RETURN_API_PATH, request, String.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    private JSONObject createReturnRequestFromAFinalizedOrderBody() {
        JSONArray returnedItems =
                jsonArray()
                        .put(json()
                                .put("order_item_id", 330042)
                                .put("returned_quantity", 1));
        return json().put("item_list", returnedItems);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void customerCreateReturnRequestItemsNotFromSameOrderTest() {
        JSONObject body = createReturnRequestWithItemsFromDifferentOrdersBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_ORDER_RETURN_API_PATH, request, String.class);
        //mapper.readValue(response.getBody(), ErrorResponseDTO.class);
        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    private JSONObject createReturnRequestWithItemsFromDifferentOrdersBody() {
        JSONArray returnedItems =
                jsonArray()
                        .put(json()
                                .put("order_item_id", 330031)
                                .put("returned_quantity", 1))
                        .put(json()
                                .put("order_item_id", 330036)
                                .put("returned_quantity", 1));
        return json().put("item_list", returnedItems);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void customerCreateReturnRequestZeroQuantitiesTest() {
        JSONObject body = createReturnRequestWithZeroQuantityBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_ORDER_RETURN_API_PATH, request, String.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    private JSONObject createReturnRequestWithZeroQuantityBody() {
        JSONArray returnedItems =
                jsonArray()
                        .put(json()
                                .put("order_item_id", 330031)
                                .put("returned_quantity", 0))
                        .put(json()
                                .put("order_item_id", 330033)
                                .put("returned_quantity", 1));
        return json().put("item_list", returnedItems);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void customerCreateReturnRequestTooMuchQuantityTest() {
        JSONObject body = createReturnRequestWithTooMuchQuantityBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_ORDER_RETURN_API_PATH, request, String.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    private JSONObject createReturnRequestWithTooMuchQuantityBody() {
        JSONArray returnedItems =
                jsonArray()
                        .put(json()
                                .put("order_item_id", 330033)
                                .put("returned_quantity", 1))
                        .put(json()
                                .put("order_item_id", 330034)
                                .put("returned_quantity", 1));
        return json().put("item_list", returnedItems);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void customerCreateReturnRequestItemsOfAnotherCustomerTest() {
        JSONObject body = createReturnRequestWithAnotherCustomerItemsBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_ORDER_RETURN_API_PATH, request, String.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    private JSONObject createReturnRequestWithAnotherCustomerItemsBody() {
        JSONArray returnedItems =
                jsonArray()
                        .put(json()
                                .put("order_item_id", 330031)
                                .put("returned_quantity", 1000))
                        .put(json()
                                .put("order_item_id", 330033)
                                .put("returned_quantity", 1));
        return json().put("item_list", returnedItems);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void customerCreateReturnRequestWithNoExistingItemsTest() {
        JSONObject body = createReturnRequestWithNonExistingItemsBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_ORDER_RETURN_API_PATH, request, String.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    private JSONObject createReturnRequestWithNonExistingItemsBody() {
        JSONArray returnedItems =
                jsonArray()
                        .put(json()
                                .put("order_item_id", -1)
                                .put("returned_quantity", 1000))
                        .put(json()
                                .put("order_item_id", 330032)
                                .put("returned_quantity", 1));
        return json().put("item_list", returnedItems);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void customerCreateReturnRequestWithAlreadyReturnedMetaOrderTest() {
        JSONObject body = createReturnRequestWithReturnedItemsBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_ORDER_RETURN_API_PATH, request, String.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    private JSONObject createReturnRequestWithReturnedItemsBody() {
        JSONArray returnedItems =
                jsonArray()
                        .put(json()
                                .put("order_item_id", 330036)
                                .put("returned_quantity", 1));
        return json().put("item_list", returnedItems);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void customerCreateReturnRequestWithNonFinalizedMetaOrderTest() {
        JSONObject body = createReturnRequestWithNonFinalizedMetaOrderBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity(YESHTERY_ORDER_RETURN_API_PATH, request, String.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    private JSONObject createReturnRequestWithNonFinalizedMetaOrderBody() {
        JSONArray returnedItems =
                jsonArray()
                        .put(json()
                                .put("order_item_id", 330041)
                                .put("returned_quantity", 1));
        return json().put("item_list", returnedItems);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void rejectReturnOrderRequest() {
        JSONObject body = json().put("return_request_id", 330031)
                .put("rejection_reason", "damaged product");
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_RETURN_REJECT_API_PATH, request, String.class);
        Assert.assertEquals(200, res.getStatusCodeValue());

        Optional<ReturnRequestEntity> entity = returnRequestRepo.findByIdAndOrganizationIdAndStatus(330031L, 99001L, REJECTED.getValue());
        assertTrue(entity.isPresent());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void rejectReturnOrderRequestConfirmedRequest() {
        Long id = 330032L;

        Optional<ReturnRequestEntity> entity =
                returnRequestRepo.findByIdAndOrganizationIdAndStatus(
                        id,
                        99001L,
                        CONFIRMED.getValue());
        assertTrue(entity.isPresent());

        JSONObject body = json().put("return_request_id", id)
                .put("rejection_reason", "damaged product");
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_RETURN_REJECT_API_PATH, request, String.class);


        Assert.assertEquals(200, res.getStatusCodeValue());

        entity = returnRequestRepo.findByIdAndOrganizationIdAndStatus(id, 99001L, REJECTED.getValue());
        assertTrue(entity.isPresent());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void rejectReturnOrderRequestReceivedRequest() {
        Long returnRequestWithReceivedStatusId = 440034L;
        Long returnRequestWIthNewStatusId = 440035L;

        // Test conversion from RECEIVED to REJECTED
        Optional<ReturnRequestEntity> entity =
                returnRequestRepo.findByIdAndOrganizationIdAndStatus(
                        returnRequestWithReceivedStatusId,
                        99001L,
                        RECEIVED.getValue());
        assertTrue(entity.isPresent());


        JSONObject rightBody = json()
                .put("return_request_id", returnRequestWithReceivedStatusId)
                .put("rejection_reason", "damaged product");
        HttpEntity<?> request = getHttpEntity(rightBody.toString(), "131415");

        ResponseEntity<String> res = template.postForEntity(
                YESHTERY_ORDER_RETURN_REJECT_API_PATH,
                request,
                String.class);


        Assert.assertEquals(406, res.getStatusCodeValue());

        // Test with wrong return request id
        JSONObject wrongBody = json()
                .put("return_request_id", 50555L)
                .put("rejection_reason", "damaged product");
        request = getHttpEntity(wrongBody.toString(), "131415");

        res = template.postForEntity(
                YESHTERY_ORDER_RETURN_REJECT_API_PATH,
                request,
                String.class);

        Assert.assertEquals(406, res.getStatusCodeValue());

        // Successful
        entity =
                returnRequestRepo.findByIdAndOrganizationIdAndStatus(
                        returnRequestWIthNewStatusId,
                        99001L,
                        ReturnRequestStatus.NEW.getValue());
        assertTrue(entity.isPresent());


        rightBody = json()
                .put("return_request_id", returnRequestWIthNewStatusId)
                .put("rejection_reason", "damaged product");
        request = getHttpEntity(rightBody.toString(), "131415");

        res = template.postForEntity(
                YESHTERY_ORDER_RETURN_REJECT_API_PATH,
                request,
                String.class);


        // TODO: Uncomment below assertion after fixing ThymeleafTemplateMail
        // Assert.assertEquals(200, res.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void confirmReturnRequestAuthNTest() {
        var id = 450002L;
        HttpEntity<?> request = getHttpEntity("INVALID");

        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_RETURN_CONFIRM_API_PATH + "?id=" + id, request, String.class);
        Assert.assertEquals(401, res.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void confirmReturnRequestAuthZTest() {
        var id = 450001L;
        HttpEntity<?> request = getHttpEntity("101112");

        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_RETURN_CONFIRM_API_PATH + "?id=" + id, request, String.class);
        Assert.assertEquals(403, res.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void confirmReturnRequestNonExistingRequestTest() {
        var id = -1L;
        HttpEntity<?> request = getHttpEntity("131415");

        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_RETURN_CONFIRM_API_PATH + "?id=" + id, request, String.class);
        Assert.assertEquals(406, res.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void confirmReturnRequestFromAnotherOrgTest() {
        var id = -1L;
        HttpEntity<?> request = getHttpEntity("131415");

        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_RETURN_CONFIRM_API_PATH + "?id=" + id, request, String.class);
        Assert.assertEquals(406, res.getStatusCodeValue());
    }


    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_10.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void confirmReturnRequestWithInvalidStateTest() {
        var id = 450002L;
        HttpEntity<?> request = getHttpEntity("131415");

        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_RETURN_CONFIRM_API_PATH + "?id=" + id, request, String.class);
        Assert.assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
    }

    private JSONObject createOrderStatusUpdateRequest(OrderStatus status) {
        JSONObject request = new JSONObject();
        request.put("status", status.name());
        return request;
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Shipping_Test_Data_12.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void testGetOffer() throws IOException {
        HttpEntity<?> request = getHttpEntity("123");
        ResponseEntity<String> response =
                template.exchange(YESHTERY_SHIPPING_OFFERS_API_PATH + "?customer_address=12300001", GET, request, String.class);

        Assert.assertEquals(OK, response.getStatusCode());

        List<ShippingOfferDTO> offers =
                objectMapper.readValue(response.getBody(), new TypeReference<List<ShippingOfferDTO>>() {
                });
        var allOffersAreForFixedFeeService =
                offers.stream().allMatch(offer -> Objects.equals(offer.getServiceId(), FixedFeeStrictSameCityShippingService.SERVICE_ID));
        List<ShipmentDTO> shipments = offers.get(0).getShipments();

        sort(shipments, comparing(ShipmentDTO::getShippingFee));
        assertTrue(allOffersAreForFixedFeeService);
        Assert.assertEquals(3, shipments.size());
        Assert.assertEquals(0, shipments.get(0).getShippingFee().compareTo(new BigDecimal("5")));
        Assert.assertEquals(0, shipments.get(1).getShippingFee().compareTo(new BigDecimal("5")));
        Assert.assertEquals(0, shipments.get(2).getShippingFee().compareTo(new BigDecimal("5")));
        Assert.assertEquals(now().plusDays(1).toLocalDate(), shipments.get(0).getEta().getFrom().toLocalDate());
        Assert.assertEquals(now().plusDays(2).toLocalDate(), shipments.get(0).getEta().getTo().toLocalDate());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Shipping_Test_Data_12.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void testGetOfferCustomerFromAnotherCity() throws IOException {
        HttpEntity<?> request = getHttpEntity("123");
        ResponseEntity<String> response =
                template.exchange(YESHTERY_SHIPPING_OFFERS_API_PATH + "?customer_address=12300003", GET, request, String.class);

        Assert.assertEquals(OK, response.getStatusCode());

        List<ShippingOfferDTO> offers =
                objectMapper.readValue(response.getBody(), new TypeReference<List<ShippingOfferDTO>>() {
                });
        assertTrue(offers.isEmpty());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Shipping_Test_Data_12.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void createDeliveryTest() {
        ShippingService service = shippingServiceFactory
                .getShippingService(FixedFeeStrictSameCityShippingService.SERVICE_ID, createServiceParams())
                .get();

        List<ShippingDetails> details = createShippingsDetails();

        ShipmentTracker tracker = service.requestShipment(details).collectList().block().get(0);

        assertNull(tracker.getShipmentExternalId());
        assertNull(tracker.getTracker());
        assertNull(tracker.getAirwayBillFile());
    }

    @Test(expected = RuntimeBusinessException.class)
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Shipping_Test_Data_12.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void createDeliveryCustomerFromAnotherCityTest() {
        ShippingService service = shippingServiceFactory
                .getShippingService(FixedFeeStrictSameCityShippingService.SERVICE_ID, createServiceParams())
                .get();

        List<ShippingDetails> details = createShippingsDetails();
        setShopInAnotherCity(details.get(0));

        service.requestShipment(details).collectList().block().get(0);
    }

    @Test(expected = RuntimeBusinessException.class)
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Shipping_Test_Data_12.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void createDeliveryUnsupportedCityTest() {
        ShippingService service =
                shippingServiceFactory
                        .getShippingService(FixedFeeStrictSameCityShippingService.SERVICE_ID, createServiceParams())
                        .get();

        List<ShippingDetails> details = createShippingsDetails();
        setOutOfReachCity(details.get(0));

        service.requestShipment(details).collectList().block().get(0);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Shipping_Test_Data_12.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void testGetOfferCityOutOfService() {
        ShippingService service =
                shippingServiceFactory
                        .getShippingService(FixedFeeStrictSameCityShippingService.SERVICE_ID, createServiceParams())
                        .get();

        List<ShippingDetails> details = createShippingsDetails();
        setOutOfReachCity(details.get(0));

        Mono<ShippingOffer> offer = service.createShippingOffer(details);
        assertFalse(offer.blockOptional().isPresent());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Shipping_Test_Data_12.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void testGetOfferCustomerAndShopNotInSameCity() {
        ShippingService service =
                shippingServiceFactory
                        .getShippingService(FixedFeeStrictSameCityShippingService.SERVICE_ID, createServiceParams())
                        .get();

        List<ShippingDetails> details = createShippingsDetails();
        setShopInAnotherCity(details.get(0));

        Mono<ShippingOffer> offer = service.createShippingOffer(details);
        assertFalse(offer.blockOptional().isPresent());
    }

    private void setOutOfReachCity(ShippingDetails details) {
        ShippingAddress farFarAwayAddr = new ShippingAddress();
        farFarAwayAddr.setAddressLine1("Frozen Oil st.");
        farFarAwayAddr.setArea(191919L);
        farFarAwayAddr.setBuildingNumber("777");
        farFarAwayAddr.setCity(99999L);
        farFarAwayAddr.setCountry(99999L);
        farFarAwayAddr.setId(12300004L);
        farFarAwayAddr.setName("Nasnav North Pole Branch");
        details.setDestination(farFarAwayAddr);
    }

    private void setShopInAnotherCity(ShippingDetails details) {
        ShippingAddress shopAddr = new ShippingAddress();
        shopAddr.setAddressLine1("pikutcho st.");
        shopAddr.setArea(171717L);
        shopAddr.setBuildingNumber("555");
        shopAddr.setCity(1L);
        shopAddr.setCountry(1L);
        shopAddr.setFlatNumber("5A");
        shopAddr.setId(12300001L);
        shopAddr.setName("Ash ketchum");
        details.setSource(shopAddr);
    }

    private List<ServiceParameter> createServiceParams() {
        return asList(new ServiceParameter(MIN_SHIPPING_FEE, "15")
                , new ServiceParameter(SUPPORTED_CITIES, "[1,2]")
                , new ServiceParameter(ETA_DAYS_MIN, ETA_FROM.toString())
                , new ServiceParameter(ETA_DAYS_MAX, ETA_TO.toString()));
    }

    private List<ShippingDetails> createShippingsDetails() {
        ShippingAddress customerAddr = new ShippingAddress();
        customerAddr.setAddressLine1("Mama st.");
        customerAddr.setArea(181818L);
        customerAddr.setBuildingNumber("555");
        customerAddr.setCity(2L);
        customerAddr.setCountry(1L);
        customerAddr.setFlatNumber("5A");
        customerAddr.setId(12300001L);
        customerAddr.setName("Hamada Ezzo");


        ShippingAddress shopAddr1 = new ShippingAddress();
        shopAddr1.setAddressLine1("Food court st.");
        shopAddr1.setArea(181818L);
        shopAddr1.setBuildingNumber("777");
        shopAddr1.setCity(2L);
        shopAddr1.setCountry(1L);
        shopAddr1.setId(12300002L);
        shopAddr1.setName("7rnksh Nasnav");


        ShippingAddress shopAddr2 = new ShippingAddress();
        shopAddr2.setAddressLine1("Food court st.");
        shopAddr2.setArea(181818L);
        shopAddr2.setBuildingNumber("888");
        shopAddr2.setCity(2L);
        shopAddr2.setCountry(1L);
        shopAddr2.setId(12300003L);
        shopAddr2.setName("Freska Nasnav");

        ShipmentReceiver receiver = new ShipmentReceiver();
        receiver.setFirstName("Sponge");
        receiver.setLastName("Bob");
        receiver.setPhone("01000000000");

        ShipmentItems item1 = new ShipmentItems(601L);
        item1.setName("Cool And Pool");
        item1.setBarcode("13AB");
        item1.setProductCode("Coco");
        item1.setSpecs("Cool/XXXL");

        ShipmentItems item2 = new ShipmentItems(602L);
        item2.setName("Sponge And Bob");
        item2.setBarcode("447788888888");
        item2.setProductCode("Bob");
        item2.setSpecs("Wet/S");

        List<ShipmentItems> itemsOfShop1 = asList(item1, item2);


        ShippingDetails shippingDetails1 = new ShippingDetails();
        shippingDetails1.setDestination(customerAddr);
        shippingDetails1.setSource(shopAddr1);
        shippingDetails1.setItems(itemsOfShop1);
        shippingDetails1.setReceiver(receiver);
        shippingDetails1.setShopId(503L);
        shippingDetails1.setMetaOrderId(145L);
        shippingDetails1.setSubOrderId(100L);


        List<ShippingDetails> details = asList(shippingDetails1);
        return details;
    }

    @Before
    public void setup() {
        config.mailDryRun = true;
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Before
    public void clearCache() {
        adminService.invalidateCaches();
    }

    @Before
    public void setupLoginUser() {
        if (organization == null) {
            organization = organizationRepository.findOneById(99001L);
        }

        persistentUser = ofNullable(organization)
                .map(o -> userRepository.getByEmailAndOrganizationId("unavailable@nasnav.com", o.getId()))
                .orElse(null);

        //persistentUser = userRepository.getByEmailAndOrganizationId("unavailable@nasnav.com", organization.getId());
        if (persistentUser == null) {
            persistentUser = createUser();

            persistentUser = userRepository.save(persistentUser);
        }

    }

    private UserEntity createUser() {
        UserEntity persistentUser = new UserEntity();
        persistentUser.setName("John Smith");
        persistentUser.setEmail("unavailable@nasnav.com");
        persistentUser.setEncryptedPassword("---");
        persistentUser.setOrganizationId(organization.getId());
        persistentUser.setUserStatus(ACTIVATED.getValue());

        return persistentUser;
    }


    @Test
    public void getCategoriesTree() throws JsonProcessingException {
        var response = template.getForEntity("/v1/yeshtery/categories", String.class);
        assertEquals(OK, response.getStatusCode());

        var rootLevel = mapper.readValue(response.getBody(), new TypeReference<List<CategoryDto>>() {
        });
        assertRootLevelCategoriesReturned(rootLevel);
        assertCategoryHadImgInMetadata(rootLevel);
        assertFirstLevelCategoriesReturned(rootLevel);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getYeshteryBrands() throws JsonProcessingException {
        var response = template.getForEntity(YESHTERY_GET_BRANDS_API_PATH, Object.class);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        Integer numOfBrands = (Integer) body.get("totalPages");

        assertEquals(OK, response.getStatusCode());
        assertEquals(1, numOfBrands.intValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_11.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void confirmOrderTest() throws BusinessException {

        Long subOrderId_1 = 5001L; // has sub_meta_order_id = 310002 && yeshtery_meta_order_id = 310001
        Long subOrderId_2 = 5002L; // has sub_meta_order_id = 310002 && yeshtery_meta_order_id = 310001
        Long subOrderId_3 = 5003L; // has sub_meta_order_id = 310003 && yeshtery_meta_order_id = 310001

        HttpEntity<?> request = getHttpEntity("sdrf8s");
        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_CONFIRM_API_PATH + "?order_id=" + subOrderId_1, request, String.class);
        assertEquals(OK, res.getStatusCode());
        assertOrderHasStatus(subOrderId_1, STORE_CONFIRMED);
        assertMetaOrderHasStatus(subOrderId_1, FINALIZED);
        assertYeshteryMetaOrderHasStatus(subOrderId_1, FINALIZED); // yeshtery_meta_order

        request = getHttpEntity("sdfe47");
        res = template.postForEntity(YESHTERY_ORDER_CONFIRM_API_PATH + "?order_id=" + subOrderId_2, request, String.class);
        assertEquals(OK, res.getStatusCode());
        assertOrderHasStatus(subOrderId_2, STORE_CONFIRMED);
        assertMetaOrderHasStatus(subOrderId_2, STORE_CONFIRMED);
        assertYeshteryMetaOrderHasStatus(subOrderId_2, FINALIZED); // yeshtery_meta_order

        request = getHttpEntity("161718");
        res = template.postForEntity(YESHTERY_ORDER_CONFIRM_API_PATH + "?order_id=" + subOrderId_3, request, String.class);
        assertEquals(OK, res.getStatusCode());
        assertOrderHasStatus(subOrderId_3, STORE_CONFIRMED);
        assertMetaOrderHasStatus(subOrderId_3, STORE_CONFIRMED);
        assertYeshteryMetaOrderHasStatus(subOrderId_3, STORE_CONFIRMED);  // yeshtery_meta_order
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_11.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void rejectOrderTest() throws BusinessException {

        Long subOrderId_1 = 5001L; // has sub_meta_order_id = 310002 && yeshtery_meta_order_id = 310001
        Long subOrderId_2 = 5002L; // has sub_meta_order_id = 310002 && yeshtery_meta_order_id = 310001
        Long subOrderId_3 = 5003L; // has sub_meta_order_id = 310003 && yeshtery_meta_order_id = 310001

        String requestBodyJson = getOrderRejectJson(subOrderId_1).toString();
        HttpEntity<?> request = getHttpEntity(requestBodyJson, "sdrf8s");
        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_REJECT_API_PATH , request, String.class);
        assertEquals(OK, res.getStatusCode());
        assertOrderHasStatus(subOrderId_1, STORE_CANCELLED);
        assertMetaOrderHasStatus(subOrderId_1, FINALIZED);
        assertYeshteryMetaOrderHasStatus(subOrderId_1, FINALIZED); // yeshtery_meta_order

        requestBodyJson = getOrderRejectJson(subOrderId_2).toString();
        request = getHttpEntity(requestBodyJson, "sdfe47");
        res = template.postForEntity(YESHTERY_ORDER_REJECT_API_PATH , request, String.class);
        assertEquals(OK, res.getStatusCode());
        assertOrderHasStatus(subOrderId_2, STORE_CANCELLED);
        assertMetaOrderHasStatus(subOrderId_2, STORE_CANCELLED);
        assertYeshteryMetaOrderHasStatus(subOrderId_2, FINALIZED); // yeshtery_meta_order


        requestBodyJson = getOrderRejectJson(subOrderId_3).toString();
        request = getHttpEntity(requestBodyJson, "161718");
        res = template.postForEntity(YESHTERY_ORDER_REJECT_API_PATH , request, String.class);
        assertEquals(OK, res.getStatusCode());
        assertOrderHasStatus(subOrderId_3, STORE_CANCELLED);
        assertMetaOrderHasStatus(subOrderId_3, STORE_CANCELLED);
        assertYeshteryMetaOrderHasStatus(subOrderId_3, STORE_CANCELLED);  // yeshtery_meta_order
    }

    private void assertYeshteryMetaOrderHasStatus(Long subOrderId, OrderStatus expectedOrderStatus) {
        OrdersEntity order = orderRepository.findFullDataById(subOrderId).get();
        MetaOrderEntity yeshteryMetaOrder = order.getMetaOrder().getSubMetaOrder();

        assertEquals(expectedOrderStatus.ordinal(), yeshteryMetaOrder.getStatus().intValue());
    }

    private void assertMetaOrderHasStatus(Long subOrderId, OrderStatus expectedOrderStatus) {
        OrdersEntity order = orderRepository.findFullDataById(subOrderId).get();
        MetaOrderEntity metaOrder = order.getMetaOrder();

        assertEquals(expectedOrderStatus.ordinal(), metaOrder.getStatus().intValue());
    }

    private void assertOrderHasStatus(Long orderId, OrderStatus expectedOrderStatus){
        OrdersEntity order = orderRepository.findById(orderId).get();
        assertEquals(expectedOrderStatus.ordinal(), order.getStatus().intValue());
    }

    private JSONObject getOrderRejectJson(Long subOrderId){
        JSONObject requestBody =
                json()
                        .put("sub_order_id", subOrderId)
                        .put("rejection_reason", "Due to some reasons");

        return requestBody;
    }
    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_4.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void orderCancelCompleteCycle() throws BusinessException {

        addCartItems(90L, 602L, 2, 99001L);
        addCartItems(90L, 604L, 1, 99001L);

        //checkout
        JSONObject requestBody = createCartCheckoutBodyForCompleteCycleTest(null);

        Order order = checkOutCart("789", requestBody, new BigDecimal("3125.00"), new BigDecimal("3100.00"), new BigDecimal("25.00"));
        Long metaOrderId = order.getOrderId();

        orderService.finalizeOrder(metaOrderId);

        HttpEntity<?> request = getHttpEntity("789");
        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_CANCEL_API_PATH + "?meta_order_id=" + metaOrderId, request, String.class);
        Assert.assertEquals(OK, res.getStatusCode());
        assertOrderCanceled(metaOrderId);

    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_13.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void userObtainPointsThroughOrder() {

        addCartItems(90L, 601L, 1, 99001L);
        addCartItems(90L, 602L, 1, 99002L);

        JSONObject requestBody = createCartCheckoutBodyForCompleteCycleTest(null);

        Order order = checkOutCart("789", requestBody, new BigDecimal("850.00"), new BigDecimal("800.00"), new BigDecimal("50.00"));
        List<Long> orderIds = order.getSubOrders().stream().map(SubOrder::getSubOrderId).sorted().collect(toList());

        MetaOrderEntity metaOrder = metaOrderRepo.findYeshteryMetaorderByMetaOrderId(order.getOrderId()).get();
        Set<OrdersEntity> subOrders = new HashSet<>(orderRepository.findByIdIn(orderIds));

        template.exchange(YeshteryConstants.API_PATH + "/payment/cod/execute?order_id=" + order.getOrderId(), POST, null, String.class);

        confirmSubOrdersAndAssertPointsGained(orderIds);
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_13.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void userPaysUsingOrgPoints() {
        userObtainPointsThroughOrder();

        addCartItems(90L, 601L, 1, 99001L);
        addCartItems(90L, 602L, 1, 99002L);

        Set<Long> transIds = loyaltyPointTransactionRepo.findByOrganization_IdIn(List.of(99001L, 99002L))
                .stream()
                .map(LoyaltyPointTransactionEntity::getId)
                .collect(toSet());

        JSONObject requestBody = createCartCheckoutBodyForCompleteCycleTest(transIds);

        Order order = checkOutCart("789", requestBody, new BigDecimal("810.00"), new BigDecimal("800.00"), new BigDecimal("50.00"));
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_13.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void userPaysUsingYeshteryAndOrgsPoints() {
        userObtainPointsThroughOrder();

        addCartItems(90L, 601L, 1, 99001L);
        addCartItems(90L, 602L, 1, 99002L);

        Set<Long> transIds = loyaltyPointTransactionRepo.findByOrganization_IdIn(List.of(99001L, 99002L, 99003L))
                .stream()
                .map(LoyaltyPointTransactionEntity::getId)
                .collect(toSet());

        JSONObject requestBody = createCartCheckoutBodyForCompleteCycleTest(transIds);
        // paying using both yeshtery points and organization points gives double discount for current config
        Order order = checkOutCart("789", requestBody, new BigDecimal("770.00"), new BigDecimal("800.00"), new BigDecimal("50.00"));
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_13.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void listSpendablePointsInCart() {
        userObtainPointsThroughOrder();

        addCartItems(90L, 601L, 1, 99001L);
        addCartItems(90L, 602L, 1, 99002L);

        StringBuilder params = new StringBuilder();
        loyaltyPointTransactionRepo.findByOrganization_IdIn(List.of(99001L, 99002L, 99003L))
                .stream()
                .map(LoyaltyPointTransactionEntity::getId)
                .forEach(id -> params.append(id+","));
        params.deleteCharAt(params.lastIndexOf(","));

        HttpEntity<?> request = getHttpEntity("789");
        ResponseEntity<Cart> response =
                template.exchange("/v1/cart" + "?points="+params, GET, request, Cart.class);
        assertEquals(new BigDecimal("80.00"), response.getBody().getDiscount());
        assertEquals(4, response.getBody().getPoints().getAppliedPoints().size());
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Loyalty_Point_Test_Data_2.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void userGetPointsThroughReferral() {
        registerNewUser();

        UserEntity newUser = prepareNewUserInfo();

        createAndConfirmOrder(newUser.getId(), "456987");

        LoyaltyPointTransactionEntity transaction = loyaltyPointTransactionRepo.findByUser_IdAndOrganization_Id(88L, 99001L).get(0);
        Assertions.assertEquals(true, transaction.getIsValid());
    }

    private Long createAndConfirmOrder(Long userId, String token) {
        addCartItems(userId, 601L, 1, 99001L, token);
        String requestBody = createCartCheckoutBodyWithPickup().toString();
        HttpEntity request = getHttpEntity(requestBody, token);
        ResponseEntity<Order> response2 = template.postForEntity("/v1/cart/checkout/", request, Order.class);
        assertEquals(200, response2.getStatusCodeValue());

        Order resBody = response2.getBody();

        // finalize payment
        ResponseEntity res = template.postForEntity("/v1/payment/cod/execute?order_id="+ resBody.getOrderId(), null, String.class);
        assertEquals(200, res.getStatusCodeValue());

        // confirm order
        request = getHttpEntity( "abcdefg");
        ResponseEntity response3 =
                template.postForEntity("/v1/order/confirm?order_id="+resBody.getSubOrders().get(0).getSubOrderId(), request, String.class);
        assertEquals(200, response3.getStatusCodeValue());

        return resBody.getOrderId();

    }
    private void registerNewUser() {
        Long userId = 88L;
        String requestBody = json()
                .put("email", "a@b.com")
                .put("name", "test")
                .put("password", "123456")
                .put("confirmation_flag", true)
                .put("org_id", 99001)
                .put("phone_number", "4545564")
                .put("redirect_url", "https://nasnav.org/dummy_org/login")
                .toString();
        HttpEntity request = getHttpEntity(requestBody, null);
        ResponseEntity<UserApiResponse> response = template.postForEntity("/v1/user/register?referral=88", request, UserApiResponse.class);
        Assertions.assertEquals(201, response.getStatusCodeValue());
        LoyaltyPointTransactionEntity transaction = loyaltyPointTransactionRepo.findByUser_IdAndOrganization_Id(userId, 99001L).get(0);
        Assertions.assertEquals(false, transaction.getIsValid());
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Loyalty_Point_Test_Data_2.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void userGetPointsThroughShopPickup() {
        Long orderId = metaOrderRepo.findYeshteryMetaorderByMetaOrderId(createAndConfirmOrder(88L, "123"))
                .map(MetaOrderEntity::getSubMetaOrders)
                .get()
                .stream()
                .map(MetaOrderEntity::getSubOrders)
                .flatMap(Set::stream)
                .findFirst()
                .map(OrdersEntity::getId)
                .orElse(-1L);

        HttpEntity request = getHttpEntity("192021");
        ResponseEntity<String> response = template.exchange("/v1/loyalty/points/code/generate?shop_id=501", GET, request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        String code = response.getBody();

        request = getHttpEntity("123");
        response = template.postForEntity("/v1/loyalty/points/code/redeem?order_id=" + orderId + "&code="+ code, request, String.class);
        assertEquals(200, response.getStatusCodeValue());

        LoyaltyPointTransactionEntity transaction = loyaltyPointTransactionRepo.findByUser_IdAndOrganization_IdAndType(88L, 99001L, 3).get(0);
        Assertions.assertEquals(true, transaction.getIsValid());
    }

    private UserEntity prepareNewUserInfo() {
        UserEntity newUser = userRepository.findByEmailAndOrganizationId("a@b.com", 99001L).get();
        newUser.setUserStatus(201);
        newUser.setTier(tierRepo.findByIdAndOrganization_Id(1L, 99001L).get());
        newUser = userRepository.saveAndFlush(newUser);
        UserTokensEntity token = new UserTokensEntity();
        token.setUserEntity(newUser);
        token.setToken("456987");
        tokenRepository.saveAndFlush(token);

        return newUser;
    }

    private JSONObject createCartCheckoutBodyWithPickup() {
        JSONObject body = new JSONObject();
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("SHOP_ID", "501");
        body.put("customer_address", 12300001);
        body.put("shipping_service_id", "TEST");
        body.put("additional_data", additionalData);
        return body;
    }

    private void confirmSubOrdersAndAssertPointsGained(List<Long> orderIds) {
        //confirm first suborder
        HttpEntity<?> request = getHttpEntity("131415");
        Long orderId = orderIds.get(0);
        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_CONFIRM_API_PATH + "?order_id=" + orderId, request, String.class);
        assertEquals(200, res.getStatusCodeValue());
        LoyaltyPointTransactionEntity transaction1 = loyaltyPointTransactionRepo.findByOrder_Id(orderId).get();
        assertEquals(new BigDecimal("210.00"), transaction1.getPoints());

        //confirm second suborder
        request = getHttpEntity("161718");
        orderId = orderIds.get(1);
        res = template.postForEntity(YESHTERY_ORDER_CONFIRM_API_PATH + "?order_id=" + orderId, request, String.class);
        assertEquals(200, res.getStatusCodeValue());
        LoyaltyPointTransactionEntity transaction2 = loyaltyPointTransactionRepo.findByOrder_Id(orderId).get();
        assertEquals(new BigDecimal("70.00"), transaction2.getPoints());
    }

    private void addCartItems(Long userId, Long stockId, Integer quantity, Long orgId) {
        addCartItems(userId, stockId, quantity, orgId, null);
    }
    private void addCartItems(Long userId, Long stockId, Integer quantity, Long orgId, String t) {
        String token = ofNullable(t).orElse("789");
        var itemsCountBefore = cartItemRepo.countByUser_Id(userId);

        JSONObject item = createCartItem(stockId, quantity, orgId);

        HttpEntity<?> request = getHttpEntity(item.toString(), token);
        ResponseEntity<Cart> response =
                template.exchange(YESHTERY_CART_ITEM_API_PATH, POST, request, Cart.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(itemsCountBefore + 1, response.getBody().getItems().size());
    }

    private JSONObject createCartCheckoutBodyForCompleteCycleTest(Set<Long> points) {
        JSONObject body = new JSONObject();
        Map<String, String> additionalData = new HashMap<>();
        body.put("customer_address", 12300001);
        body.put("shipping_service_id", "BOSTA_LEVIS");
        body.put("additional_data", additionalData);
        body.put("notes", "come after dinner");
        body.put("points", points);
        return body;
    }

    private JSONObject createCartItem(Long stockId, Integer quantity, Long orgId) {
        JSONObject item = new JSONObject();
        item.put("stock_id", stockId);
        item.put("cover_img", "img");
        item.put("quantity", quantity);
        item.put("org_id", orgId);

        return item;
    }

    private void assertOrderCanceled(Long metaOrderId) {
        MetaOrderEntity yeshteryMetaOrder = metaOrderRepo.findYeshteryMetaorderByMetaOrderId(metaOrderId).get();
        Set<MetaOrderEntity> subMetaOrders = yeshteryMetaOrder.getSubMetaOrders();
        List<OrdersEntity> subOrders =
                subMetaOrders
                        .stream()
                        .map(MetaOrderEntity::getSubOrders)
                        .flatMap(Set::stream)
                        .collect(toList());

        Assert.assertEquals(CLIENT_CANCELLED.getValue(), yeshteryMetaOrder.getStatus());
        subMetaOrders.forEach(subMetaOrder -> Assert.assertEquals(CLIENT_CANCELLED.getValue(), subMetaOrder.getStatus()));
        subOrders.forEach(subOrder -> Assert.assertEquals(CLIENT_CANCELLED.getValue(), subOrder.getStatus()));
    }

    private Order checkOutCart(String token, JSONObject requestBody, BigDecimal total, BigDecimal subTotal, BigDecimal shippingFee) {
        HttpEntity<?> request = getHttpEntity(requestBody.toString(), token);
        ResponseEntity<Order> res = template.postForEntity(YESHTERY_CART_CHECKOUT_API_PATH, request, Order.class);
        Assert.assertEquals(200, res.getStatusCodeValue());

        Order order = res.getBody();
        BigDecimal subOrderSubtTotalSum = getSubOrderSubTotalSum(order);
        BigDecimal subOrderTotalSum = getSubOrderTotalSum(order);
        BigDecimal subOrderShippingSum = getSubOrderShippingSum(order);

        assertTrue(order.getOrderId() != null);
        Assert.assertEquals(shippingFee, order.getShipping());
        Assert.assertEquals(subTotal, order.getSubtotal());
        Assert.assertEquals(total, order.getTotal());
        Assert.assertEquals(0, order.getShipping().compareTo(subOrderShippingSum));
        Assert.assertEquals(0, order.getSubtotal().compareTo(subOrderSubtTotalSum));
        Assert.assertEquals(0, order.getTotal().compareTo(subOrderTotalSum));
        Assert.assertEquals(USELESS_NOTE, order.getNotes());
        assertItemDataJsonCreated(order);
        return order;
    }

    private BigDecimal getSubOrderTotalSum(Order order) {
        return order
                .getSubOrders()
                .stream()
                .map(SubOrder::getTotal)
                .reduce(ZERO, BigDecimal::add);
    }
    private BigDecimal getSubOrderShippingSum(Order order) {
        return order
                .getSubOrders()
                .stream()
                .map(SubOrder::getShipment)
                .map(Shipment::getShippingFee)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal getSubOrderSubTotalSum(Order order) {
        return order
                .getSubOrders()
                .stream()
                .map(SubOrder::getSubtotal)
                .reduce(ZERO, BigDecimal::add);
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
                        collectingAndThen(toList(), ids -> basketRepository.findByIdIn(ids)))
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

    private void assertFirstLevelCategoriesReturned(List<CategoryDto> rootLevel) {
        var firstLevel = getCategoriesOfFirstLevel(rootLevel);
        assertTrue(Set.of(203L, 204L, 205L, 206L).containsAll(firstLevel));
    }

    private void assertRootLevelCategoriesReturned(List<CategoryDto> rootLevel) {
        var ids = rootLevel.stream().map(CategoryDto::getId).collect(toSet());
        assertTrue(Set.of(201L, 202L).containsAll(ids));
        assertEquals(2, rootLevel.size());
    }

    private void assertCategoryHadImgInMetadata(List<CategoryDto> rootLevel) {
        rootLevel.stream().filter(c -> Objects.equals(201L, c.getId())).findFirst().ifPresent(this::hasCoverImage);
    }

    private List<Long> getCategoriesOfFirstLevel(List<CategoryDto> rootLevel) {
        return rootLevel
                .stream()
                .map(CategoryDto::getChildren)
                .flatMap(List::stream)
                .map(CategoryDto::getId)
                .collect(toList());
    }

}
