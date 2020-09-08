package com.nasnav.test;

import static com.nasnav.enumerations.ReturnRequestStatus.RECEIVED;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static com.nasnav.test.commons.TestCommons.jsonArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import com.nasnav.NavBox;
import com.nasnav.dao.ReturnRequestItemRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.persistence.ReturnRequestItemEntity;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_9.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class OrderReturnTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private StockRepository stockRepository;
    
    @Autowired
    private ReturnRequestItemRepository returnRequestItemRepo;


    @Test
    public void returnOrderItemUsingBasketItemsSuccess() {
        JSONObject basketItems = 
        		json()
                .put("order_item_id", 330034)
                .put("received_quantity", 1);

        Integer oldStockQuantity = stockRepository.findById(604L).get().getQuantity();

        JSONObject body = json().put("basket_items", jsonArray().put(basketItems));
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);

        assertEquals(200, response.getStatusCodeValue());

        Integer newStockQuantity = stockRepository.findById(604L).get().getQuantity();
        assertEquals(newStockQuantity.intValue(), oldStockQuantity.intValue() + 1);

        List<ReturnRequestItemEntity> items =  returnRequestItemRepo.findByBasket_IdIn(Arrays.asList((330034L)));
        assertTrue(!items.isEmpty());
        assertTrue(items.get(0).getReturnRequest() != null);
        assertTrue(items.get(0).getReturnRequest().getStatus().equals(RECEIVED.getValue()));
    }


    @Test
    public void returnOrderItemUsingRequestItemsSuccess() {
        JSONObject basketItems = 
        		json()
                .put("return_request_item_id", 330031)
                .put("received_quantity", 1);

        Integer oldStockQuantity = stockRepository.findById(601L).get().getQuantity();

        JSONObject body = json().put("returned_items", jsonArray().put(basketItems));
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);

        assertEquals(200, response.getStatusCodeValue());

        Integer newStockQuantity = stockRepository.findById(601L).get().getQuantity();
        assertEquals(newStockQuantity.intValue(), oldStockQuantity.intValue() + 1);

        List<ReturnRequestItemEntity> items =  returnRequestItemRepo.findByBasket_IdIn(Arrays.asList((330031L)));
        assertTrue(!items.isEmpty());
        assertTrue(items.get(0).getReturnRequest() != null);
        assertTrue(items.get(0).getReturnRequest().getStatus().equals(RECEIVED.getValue()));
    }


    @Test
    public void returnOrderUsingRequestItemsAndBasketItemsSuccess() {
        JSONArray basketItems = jsonArray().put(json().put("order_item_id", 330034).put("received_quantity", 1));
        JSONArray requestItems = jsonArray().put(json().put("return_request_item_id", 330033).put("received_quantity", 1));

        Integer oldStockQuantity = stockRepository.findById(603L).get().getQuantity();
        Integer oldStockQuantity2 = stockRepository.findById(604L).get().getQuantity();

        JSONObject body = 
        		json()
                .put("basket_items", basketItems)
                .put("returned_items", requestItems);
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);

        assertEquals(200, response.getStatusCodeValue());


        Integer newStockQuantity = stockRepository.findById(603L).get().getQuantity();
        Integer newStockQuantity2 = stockRepository.findById(604L).get().getQuantity();

        assertEquals(newStockQuantity.intValue(), oldStockQuantity.intValue() + 1);
        assertEquals(newStockQuantity2.intValue(), oldStockQuantity2.intValue() + 1);
    }


    @Test
    public void returnOrderInvalidAuthZ() {
        HttpEntity<?> request = getHttpEntity("101112");
        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);
        assertEquals(403, response.getStatusCodeValue());
    }


    @Test
    public void returnOrderInvalidAuthN() {
        HttpEntity<?> request = getHttpEntity("invalid token");
        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
    }


    @Test
    public void returnOrderInvalidInput() {
        JSONObject basketItems = 
        		json()
                .put("order_item_id", 330031);

        JSONObject body = json().put("items", basketItems);
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);

        assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void returnOrderInvalidReturnBasketItemsIds() {
        JSONObject basketItems = 
        		json()
                .put("order_item_id", 3300312)
                .put("received_quantity", 1);

        JSONObject body = json().put("basket_items", jsonArray().put(basketItems));
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);

        assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void returnOrderInvalidReturnRequestItemsIds() {
        JSONObject basketItems = 
        		json()
                .put("return_request_item_id", 3300312)
                .put("received_quantity", 1);

        JSONObject body = json().put("returned_items", jsonArray().put(basketItems));
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);

        assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void returnOrderItemDifferentBasketItemsMetaOrder() {
        JSONArray items = jsonArray().put(json()
                                        .put("order_item_id", 330035)
                                        .put("received_quantity", 1))
                                     .put(json()
                                            .put("order_item_id", 330036)
                                            .put("received_quantity", 1));

        JSONObject body = json().put("basket_items", items);
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);

        assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void returnOrderItemDifferentReturnItemsMetaOrder() {
        JSONArray basketItems = jsonArray().put(json().put("order_item_id", 330036).put("received_quantity", 1));
        JSONArray requestItems = jsonArray().put(json().put("return_request_item_id", 330031).put("received_quantity", 1));

        JSONObject body = 
        		json()
                .put("basket_items", basketItems)
                .put("returned_items", requestItems);
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);

        assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void returnOrderItemDifferentReturnItemsReturnRequest() {
        JSONArray requestItems = 
        		jsonArray()
                .put(json()
                        .put("return_request_item_id", 330031)
                        .put("received_quantity", 1))
                .put(json()
                        .put("return_request_item_id", 330033)
                        .put("received_quantity", 1));

        JSONObject body = json()
                .put("returned_items", requestItems);
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);

        assertEquals(406, response.getStatusCodeValue());
    }
    
    
    //TODO: POST/receive_items -> received quantity for basket item more than original order.
    
    
    @Test
    public void customerCreateReturnRequestNoAuthZTest() {
    	JSONObject body = createReturnRequestBody();
    			
    	HttpEntity<?> request = getHttpEntity(body.toString(), "131415");
    	ResponseEntity<String> response = template.postForEntity("/order/return", request, String.class);
    	
    	assertEquals(FORBIDDEN, response.getStatusCode());
    }


	private JSONObject createReturnRequestBody() {
		JSONArray returnedItems = 
        		jsonArray()
                .put(json()
                        .put("order_item_id", 330031)
                        .put("returned_quantity", 1))
                .put(json()
                        .put("order_item_id", 330033)
                        .put("returned_quantity", 1));
    	JSONObject body = json().put("item_list", returnedItems);
		return body;
	}
    
    
    
    
    @Test
    public void customerCreateReturnRequestNoAuthNTest() {
    	JSONObject body = createReturnRequestBody();
		
    	HttpEntity<?> request = getHttpEntity(body.toString(), "INVALID");
    	ResponseEntity<String> response = template.postForEntity("/order/return", request, String.class);
    	
    	assertEquals(UNAUTHORIZED, response.getStatusCode());
    }
    
    
    
    
    
    @Test
    public void customerCreateReturnRequestOrderPast14DaysTest() {
    	JSONObject body = createReturnRequestWithTooOldItemsBody();
		
    	HttpEntity<?> request = getHttpEntity(body.toString(), "123");
    	ResponseEntity<String> response = template.postForEntity("/order/return", request, String.class);
    	
    	assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }
    
    
    

    private JSONObject createReturnRequestWithTooOldItemsBody() {
		JSONArray returnedItems = 
        		jsonArray()
                .put(json()
                        .put("order_item_id", 330035)
                        .put("returned_quantity", 1));
    	JSONObject body = json().put("item_list", returnedItems);
		return body;
	}
    
    
    
    
    @Test
    public void customerCreateReturnRequestItemsNotFromSameOrderTest() {
    	JSONObject body = createReturnRequestWithItemsFromDifferentOrdersBody();
		
    	HttpEntity<?> request = getHttpEntity(body.toString(), "123");
    	ResponseEntity<String> response = template.postForEntity("/order/return", request, String.class);
    	
    	assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
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
    	JSONObject body = json().put("item_list", returnedItems);
		return body;
	}
    
    
    
    @Test
    public void customerCreateReturnRequestZeroQuantitiesTest() {
    	JSONObject body = createReturnRequestWithZeroQuantityBody();
		
    	HttpEntity<?> request = getHttpEntity(body.toString(), "123");
    	ResponseEntity<String> response = template.postForEntity("/order/return", request, String.class);
    	
    	assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
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
    	JSONObject body = json().put("item_list", returnedItems);
		return body;
	}
    
    
    
    @Test
    public void customerCreateReturnRequestTooMuchQuantityTest() {
    	JSONObject body = createReturnRequestWithTooMuchQuantityBody();
		
    	HttpEntity<?> request = getHttpEntity(body.toString(), "123");
    	ResponseEntity<String> response = template.postForEntity("/order/return", request, String.class);
    	
    	assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
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
    	JSONObject body = json().put("item_list", returnedItems);
		return body;
	}
    
    
    
    
    
    @Test
    public void customerCreateReturnRequestItemsOfAnotherCustomerTest() {
    	JSONObject body = createReturnRequestWithAnotherCustomerItemsBody();
		
    	HttpEntity<?> request = getHttpEntity(body.toString(), "123");
    	ResponseEntity<String> response = template.postForEntity("/order/return", request, String.class);
    	
    	assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
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
    	JSONObject body = json().put("item_list", returnedItems);
		return body;
	}
    
    
    
    
    @Test
    public void customerCreateReturnRequestWithNoExistingItemsTest() {
    	JSONObject body = createReturnRequestWithNonExistingItemsBody();
		
    	HttpEntity<?> request = getHttpEntity(body.toString(), "123");
    	ResponseEntity<String> response = template.postForEntity("/order/return", request, String.class);
    	
    	assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
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
    	JSONObject body = json().put("item_list", returnedItems);
		return body;
	}
    
    
    
    
    
    @Test
    public void customerCreateReturnRequestWithAlreadyReturnedMetaOrderTest() {
    	JSONObject body = createReturnRequestWithReturnedItemsBody();
		
    	HttpEntity<?> request = getHttpEntity(body.toString(), "123");
    	ResponseEntity<String> response = template.postForEntity("/order/return", request, String.class);
    	
    	assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }
    
    
    
    
    private JSONObject createReturnRequestWithReturnedItemsBody() {
		JSONArray returnedItems = 
        		jsonArray()
                .put(json()
                        .put("order_item_id", 330036)
                        .put("returned_quantity", 1));
    	JSONObject body = json().put("item_list", returnedItems);
		return body;
	}
    
    
    
    
    @Test
    public void customerCreateReturnOrderTest() {
    	JSONObject body = createReturnRequestBody();
		
    	HttpEntity<?> request = getHttpEntity(body.toString(), "123");
    	ResponseEntity<String> response = template.postForEntity("/order/return", request, String.class);
    	
    	assertEquals(OK, response.getStatusCode());
    }
}
