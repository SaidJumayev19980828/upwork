import com.nasnav.NavBox;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.response.OrderResponse;
import com.nasnav.response.exception.OrderValidationException;
import com.nasnav.service.OrderServiceImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:database.properties")
@RunWith(MockitoJUnitRunner.Silent.class)
public class OrderServiceTest {

    @Mock
    private OrdersRepository orderRepository;
    
    @InjectMocks
    OrderServiceImpl orderService;
   
    @Test
    public void updateOrderNewOrderSuccessTest() {
    	
    	String orderJson = "{\n" + 
    			"  \"status\": 5,\n" + 
    			"  \"basket\" : \"basket\",\n" + 
    			"  \"address\" : \"address\"\n" + 
    			"}";
    	
    	OrderResponse serviceResponse = orderService.updateOrder(orderJson);
    	
    	assertEquals(200, serviceResponse.getCode().value());
    }
    
    @Test(expected = OrderValidationException.class)
    public void updateOrderInvalidOrderJsonTest() {
    	
    	String orderJson = "";
    	
    	orderService.updateOrder(orderJson);
    }
    
    @Test
    public void updateOrderNewStatusEmptyBasketTest() {
    	
    	String orderJson = "{\n" + 
    			"  \"id\": 5,\n" + 
    			"  \"status\": 0,\n" + 
    			"  \"basket\" : \"\",\n" + 
    			"  \"address\" : \"address\"\n" + 
    			"}";
    	
    	OrdersEntity entity = new OrdersEntity();
    	
    	entity.setId((long) 2);
    	
    	when(orderRepository.findById((long) 2)).thenReturn(Optional.of(entity));
    	OrderResponse serviceResponse = orderService.updateOrder(orderJson);
    	
    	assertEquals(406, serviceResponse.getCode().value());
    }
    
}
