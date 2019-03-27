import com.nasnav.NavBox;
import com.nasnav.controller.OrdersController;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ApiResponse;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.UserService;
import org.junit.After;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class OrderServiceTest {

    private static String _authToken = "TestAuthToken";
    private long _testUserId = 0;

    private MockMvc mockMvc;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrdersRepository orderRepository;

    @Autowired
    UserService userService;


    @Mock
    private OrdersController ordersController;


    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(ordersController).build();
        // create user for test purposes
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register",
                TestCommons.getHttpEntity("{\"name\":\"Ahmed\", \"email\":\"user@nasnav.com\"}"),
                ApiResponse.class);
        _testUserId = response.getBody().getEntityId();
        System.out.println("user id is: " + _testUserId);
        Assert.assertTrue(response.getBody().isSuccess());
        Assert.assertEquals(200,response.getStatusCode().value());

        UserEntity user = userRepository.findById(_testUserId).get();
        user.setAuthenticationToken(_authToken);
//        userRepository.flush();
    }

    @After
    public void cleanup() {
        userService.deleteUser(_testUserId);
    }

    //@Test
    public void unregisteredUser()  {

        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/order/update",
                TestCommons.getHttpEntity(
                        "{ \"basket\": [ { \"product\": 1234, \"quantity\": 4} ] }"
                        , 1, "XX"), ApiResponse.class);
        //Delete this user
//        userService.deleteUser(response.getBody().getEntityId());
//        Assert.assertTrue(response.getBody().isSuccess());
        Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(),response.getStatusCode().value());
    }

    @Test
    public void createNewBasket()  {

        ResponseEntity<OrderResponse> response = template.postForEntity(
                "/order/update",
                TestCommons.getHttpEntity(
                        "{ \"basket\": [{ \"product\": 1234, \"quantity\": 4}] }"
                        , _testUserId, _authToken), OrderResponse.class);

        Assert.assertEquals(HttpStatus.OK.value(),response.getStatusCode().value());
        Assert.assertTrue(response.getBody().isSuccess());
        
        //delete the order after assertion
        orderRepository.deleteById(response.getBody().getOrderId());
    }
    
    @Test
    public void addOrderNewStatusEmptyBasket()  {

        ResponseEntity<OrderResponse> response = template.postForEntity(
                "/order/update",
                TestCommons.getHttpEntity(
                        "{ \"status\": \"NEW\", \"basket\": [] }"
                        , _testUserId, _authToken), OrderResponse.class);

        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(),response.getStatusCode().value());
        Assert.assertFalse(response.getBody().isSuccess());
    }
    
    @Test
    public void updateOrderSuccessTest()  {
    	// create a new order, then take it's oder id and try to make an update using it
        ResponseEntity<OrderResponse> response = template.postForEntity(
                "/order/update",
                TestCommons.getHttpEntity(
                        "{\"basket\": [{ \"product\": 1234, \"quantity\": 4}] }"
                        , _testUserId, _authToken), OrderResponse.class);

        Assert.assertEquals(HttpStatus.OK.value(),response.getStatusCode().value());
        Assert.assertTrue(response.getBody().isSuccess());
        
        // get the returned orderId
        long orderId = response.getBody().getOrderId();
        
        // make a new request using the created order
        response = template.postForEntity(
                "/order/update",
                TestCommons.getHttpEntity(
                        "{\"id\":" + orderId + ", \"status\" : \"CLIENT_CONFIRMED\", \"basket\": [{ \"product\": 1234, \"quantity\": 4}] }"
                        , _testUserId, _authToken), OrderResponse.class);

        Assert.assertEquals(HttpStatus.OK.value(),response.getStatusCode().value());
        Assert.assertTrue(response.getBody().isSuccess());
        
        //delete the order after assertion
        orderRepository.deleteById(orderId);      
    }
    
    @Test
    public void updateOrderNonExistingOrderIdTest()  {
    	// try updating with a non-existing order number
        ResponseEntity<OrderResponse> response = template.postForEntity(
                "/order/update",
                TestCommons.getHttpEntity(
                        "{\"id\": 250, \"status\" : \"CLIENT_CONFIRMED\", \"basket\": [{ \"product\": 1234, \"quantity\": 4}] }"
                        , _testUserId, _authToken), OrderResponse.class);

        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(),response.getStatusCode().value());
        Assert.assertFalse(response.getBody().isSuccess());  
    }
    
    @Test
    public void createOrderNonNewStatusTest()  {
    	// try updating with a non-existing order number
        ResponseEntity<OrderResponse> response = template.postForEntity(
                "/order/update",
                TestCommons.getHttpEntity(
                        "{\"status\" : \"CLIENT_CONFIRMED\", \"basket\": [{ \"product\": 1234, \"quantity\": 4}] }"
                        , _testUserId, _authToken), OrderResponse.class);

        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(),response.getStatusCode().value());
        Assert.assertEquals(OrderFailedStatus.INVALID_STATUS,response.getBody().getStatus());
        Assert.assertFalse(response.getBody().isSuccess());  
    }
    
    @Test
    public void updateOrderNonExistingStatusTest()  {
    	// try updating with a non-existing order number
        ResponseEntity<OrderResponse> response = template.postForEntity(
                "/order/update",
                TestCommons.getHttpEntity(
                        "{\"status\" : \"NON_EXISTING_STATUS\", \"basket\": [{ \"product\": 1234, \"quantity\": 4}] }"
                        , _testUserId, _authToken), OrderResponse.class);

        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(),response.getStatusCode().value());
        Assert.assertEquals(OrderFailedStatus.INVALID_STATUS,response.getBody().getStatus());
        Assert.assertFalse(response.getBody().isSuccess());  
    }

}