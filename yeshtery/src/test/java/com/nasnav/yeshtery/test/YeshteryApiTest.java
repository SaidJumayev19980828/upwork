package com.nasnav.yeshtery.test;

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
import com.nasnav.dto.response.ReturnRequestDTO;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.ReturnRequestsResponse;
import com.nasnav.service.AdminService;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.ShippingServiceFactory;
import com.nasnav.shipping.model.*;
import com.nasnav.shipping.services.FixedFeeStrictSameCityShippingService;
import com.nasnav.yeshtery.Yeshtery;
import com.nasnav.yeshtery.YeshteryConstants;
import com.nasnav.yeshtery.controller.v1.YeshteryUserController;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
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
import static com.nasnav.enumerations.ReturnRequestStatus.*;
import static com.nasnav.enumerations.UserStatus.ACTIVATED;
import static com.nasnav.shipping.services.FixedFeeShippingService.*;
import static com.nasnav.yeshtery.test.commons.TestCommons.*;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
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
public class YeshteryApiTest {
    private final String PRODUCT_FEATURE_1_NAME = "Lispstick Color";
    private final String PRODUCT_FEATURE_1_P_NAME = "lipstick_color";
    private final String PRODUCT_FEATURE_2_NAME = "Lipstick flavour";
    private final String PRODUCT_FEATURE_2_P_NAME = "lipstick_flavour";

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
    private MetaOrderRepository metaOrderRepo;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private OrdersRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeUserRepository empRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AdminService adminService;


    // Helpers & Services
    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReturnRequestRepository returnRequestRepo;

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
        assertTrue(variantFeatures.similar(expectedVariantFeatures));
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
        ResponseEntity<List> tags = template.getForEntity("/v1/yeshtery/tags?org_id=99001", List.class);

        assertTrue(!tags.getBody().isEmpty());
        Assert.assertEquals(2, tags.getBody().size());
        System.out.println(tags.getBody().toString());
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

        HttpEntity<?> httpEntity = getHttpEntity("101112");

        // no filters
        ResponseEntity<String> response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?details_level=3"
                , GET
                , httpEntity
                , String.class);

        JSONArray body = new JSONArray(response.getBody());
        var count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("all orders ", 16, count);

        //---------------------------------------------------------------------
        // by org_id
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?org_id=99001&details_level=3"
                , GET
                , httpEntity
                , String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("8 orders with org_id = 99001", 8, count);

        //---------------------------------------------------------------------
        // by shop_id
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?shop_id=501&details_level=3"
                , GET
                , httpEntity
                , String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("3 orders with shop_id = 501", 3, count);

        //---------------------------------------------------------------------
        // by user_id
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?user_id=88&details_level=3"
                , GET
                , httpEntity
                , String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("6 orders with user_id = 88", 6, count);

        //---------------------------------------------------------------------
        // by status
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?status=NEW&details_level=3"
                , GET
                , httpEntity
                , String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("4 orders with status = NEW", 4, count);

        //---------------------------------------------------------------------
        // by org_id and status
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?org_id=99001&status=NEW&details_level=3"
                , GET
                , httpEntity
                , String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("1 orders with org_id = 99001 and status = NEW", 1, count);

        //---------------------------------------------------------------------
        // by org_id and shop_id
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?org_id=99001&shop_id=503&details_level=3"
                , GET
                , httpEntity
                , String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        //---------------------------------------------------------------------
        // by org_id and user_id
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?org_id=99002&user_id=90&details_level=3"
                , GET
                , httpEntity
                , String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("1 order with org_id = 99002 and user_id = 90", 1, count);

        //---------------------------------------------------------------------
        // by shop_id and status
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?shop_id=501&status=NEW&details_level=3"
                , GET
                , httpEntity
                , String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("1 orders with shop_id = 501 and status = NEW", 1, count);


        //---------------------------------------------------------------------
        // by user_id, shop_id and status
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?user_id=88&shop_id=501&status=STORE_PREPARED&details_level=3"
                , GET
                , httpEntity
                , String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("1 order with user_id = 88 and shop_id = 501 and status = NEW", 1, count);
    }

    @Test // Organization roles diffterent filters test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void ordersListOrganizationDifferentFiltersTest() {
        ResponseEntity<String> response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?details_level=3"
                , GET
                , getHttpEntity("161718")
                , String.class);
        JSONArray body = new JSONArray(response.getBody());
        var count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("user#70 is Organization employee in org#99001 so he can view all orderes within org#99001", 8, count);
        //-------------------------------------------------------------------------

        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?details_level=3"
                , GET
                , getHttpEntity("131415")
                , String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        long org99002Orders = orderRepository.countByOrganizationEntity_id(99002L);
        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("user#69 is Organization admin in org#99002 so he can view all orderes within org#99002", org99002Orders, count);

        //-------------------------------------------------------------------------
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?details_level=3"
                , GET
                , getHttpEntity("192021")
                , String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        var shopEmpId = 71L;
        var shopId = empRepository.findById(shopEmpId)
                .map(EmployeeUserEntity::getShopId)
                .get();
        long shopOrdersCount = orderRepository.countByShopsEntity_id(shopId);
        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals(format("user#%d is store employee in store#%d so he can view all orderes within the store", shopEmpId, shopId)
                , shopOrdersCount
                , count);
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
        // by shop_id only
        ResponseEntity<String> response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?shop_id=550", GET,
                new HttpEntity<>(getHeaders("101112")), String.class);
        JSONArray body = new JSONArray(response.getBody());
        var count = body.length();
        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("No orders with shop_id = 550 ", 0, count);

        // by user_id
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?user_id=99", GET, new HttpEntity<>(getHeaders("101112")), String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("no orders with user_id = 99", 0, count);

        // by org_id
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?org_id=999999", GET, new HttpEntity<>(getHeaders("101112")), String.class);
        body = new JSONArray(response.getBody());
        count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("no orders with org_id = 999999", 0, count);

        // by status
        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?status=invalid_status", GET,
                new HttpEntity<>(getHeaders("101112")), String.class);

        assertTrue(400 == response.getStatusCode().value());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void testDateFilteration() {
        modifyOrderUpdateTime(330044L, LocalDateTime.of(2017, 11, 26, 10, 00, 00));
        modifyOrderUpdateTime(330045L, LocalDateTime.of(2017, 12, 15, 10, 00, 00));
        modifyOrderUpdateTime(330046L, LocalDateTime.of(2017, 12, 16, 10, 00, 00));

        //-------------------------------------------------------------------
        // by shop_id only
        ResponseEntity<String> response =
                template.exchange(
                        YESHTERY_ORDER_LIST_API_PATH + "?updated_before=2017-12-23:12:12:12"
                                + "&updated_after=2017-12-01:12:12:12"
                        , GET
                        , getHttpEntity("101112")
                        , String.class);

        JSONArray body = new JSONArray(response.getBody());
        var count = body.length();

        assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals("expected 2 orders to be within this given time range ", 2, count);
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

        ResponseEntity<String> response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?details_level=2&count=1", GET,
                new HttpEntity<>(getHeaders("101112")), String.class);

        DetailedOrderRepObject body = getOrderListDetailedObject(response).get(0);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(body.getTotalQuantity() != null);
        assertTrue(body.getTotalQuantity() == 3);
        Assert.assertEquals(null, body.getItems());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Order_Info_Test.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListCountTest() throws IOException {

        @SuppressWarnings("rawtypes")
        ResponseEntity<List> response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?count=1", GET,
                new HttpEntity<>(getHeaders("101112")), List.class);

        Assert.assertEquals(1, response.getBody().size());

        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?count=2", GET,
                new HttpEntity<>(getHeaders("101112")), List.class);

        Assert.assertEquals(2, response.getBody().size());

        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?count=4", GET,
                new HttpEntity<>(getHeaders("101112")), List.class);

        Assert.assertEquals(4, response.getBody().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Order_Info_Test.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrderListStartTest() throws IOException {
        //count=1&

        ResponseEntity<String> response = template.exchange(
                YESHTERY_ORDER_LIST_API_PATH + "?start=1&count=1&details_level=3",
                GET,
                new HttpEntity<>(getHeaders("101112")),
                String.class);

        DetailedOrderRepObject actualBody = getOrderListDetailedObject(response).get(0);
        DetailedOrderRepObject expectedBody = createExpectedOrderInfo(330005L, new BigDecimal("50.00"), 1, "NEW", 89L, ZERO);

        Assert.assertEquals(expectedBody, actualBody);

        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?start=2&count=1&details_level=3", GET,
                new HttpEntity<>(getHeaders("101112")), String.class);

        actualBody = getOrderListDetailedObject(response).get(0);

        expectedBody = createExpectedOrderInfo(330003L, new BigDecimal("300.00"), 7, "NEW", 88L, ZERO);
        Assert.assertEquals(expectedBody, actualBody);

        response = template.exchange(YESHTERY_ORDER_LIST_API_PATH + "?start=3&count=1&details_level=3", GET,
                new HttpEntity<>(getHeaders("101112")), String.class);

        actualBody = getOrderListDetailedObject(response).get(0);

        expectedBody = createExpectedOrderInfo(330004L, new BigDecimal("200.00"), 5, "NEW", 89L, new BigDecimal("50.00"));
        Assert.assertEquals(expectedBody, actualBody);

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
        Assert.assertEquals(FORBIDDEN, response.getStatusCodeValue());
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
        Assert.assertEquals(FORBIDDEN, response.getStatusCodeValue());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_9.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getReturnRequestInvalidAuthentication() {
        HttpEntity<?> request = getHttpEntity("invalid token");
        ResponseEntity<String> response = template.exchange(YESHTERY_ORDER_RETURN_REQUESTS_API_PATH + "?id=330031", GET, request, String.class);
        Assert.assertEquals(401, response.getStatusCodeValue());
    }

    // TODO: Fix template "order_return_notification_template.html" to make it run successfully
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

    // TODO: Fix template "order_return_reject_template.html" to make it run successfully
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

    // TODO: Fix template "order_return_reject_template.html" to make it run successfully
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
                        NEW.getValue());
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
        Assert.assertEquals(406, res.getStatusCodeValue());
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


    // TODO: Fix template "order_return_confirm_template.html" to make it run successfully
    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_10.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void confirmReturnRequestWithInvalidStateTest() {
        var id = 450002L;
        HttpEntity<?> request = getHttpEntity("131415");

        ResponseEntity<String> res = template.postForEntity(YESHTERY_ORDER_RETURN_CONFIRM_API_PATH + "?id=" + id, request, String.class);
        Assert.assertEquals(NOT_ACCEPTABLE, res.getStatusCodeValue());
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

        persistentUser = Optional.ofNullable(organization)
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
/*
    @Test
    public void getYeshteryBrands() throws JsonProcessingException {
        var response = template.exchange(
                "/v1/yeshtery/brands",
                GET,
                null,
                new ParameterizedTypeReference<RestResponsePage<Organization_BrandRepresentationObject>>() {});
        assertEquals(200, response.getStatusCodeValue());
        var body = response.getBody();
        assertEquals(1, body.getTotalPages());
        assertEquals(102, body.get().findFirst().get().getId().intValue());
    }    */

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
