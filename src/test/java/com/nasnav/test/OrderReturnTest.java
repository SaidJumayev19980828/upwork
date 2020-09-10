package com.nasnav.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.ReturnRequestItemRepository;
import com.nasnav.dao.ReturnRequestRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.response.ReturnRequestDTO;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.ReturnRequestEntity;
import com.nasnav.persistence.ReturnRequestItemEntity;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.nasnav.enumerations.ReturnRequestStatus.NEW;
import static com.nasnav.enumerations.ReturnRequestStatus.RECEIVED;
import static com.nasnav.test.commons.TestCommons.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;





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
    private ObjectMapper mapper;

    @Autowired
    private StockRepository stockRepository;
    
    @Autowired
    private ReturnRequestItemRepository returnRequestItemRepo;

    @Autowired
    private ReturnRequestRepository returnRequestRepo;

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
        assertEquals(oldStockQuantity.intValue() + 1, newStockQuantity.intValue() );

        List<ReturnRequestItemEntity> items =  returnRequestItemRepo.findByBasket_IdIn(asList((330034L)));
        assertFalse(items.isEmpty());
        assertNotNull(items.get(0).getReturnRequest());
        assertEquals(items.get(0).getReturnRequest().getStatus(), RECEIVED.getValue());
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

        List<ReturnRequestItemEntity> items =  returnRequestItemRepo.findByBasket_IdIn(asList((330031L)));
        assertFalse(items.isEmpty());
        assertNotNull(items.get(0).getReturnRequest());
        assertEquals(items.get(0).getReturnRequest().getStatus(), RECEIVED.getValue());
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
	
	
	
	
    @Test
    public void getReturnRequests() throws IOException {
        HttpEntity<?> request = getHttpEntity( "131415");
        ResponseEntity<String> response = template.exchange("/order/return/requests", GET, request, String.class);
        List<ReturnRequestDTO> body = mapper.readValue(response.getBody(), new TypeReference<List<ReturnRequestDTO>>(){});
        assertEquals(200,response.getStatusCodeValue());
        assertEquals(3, body.size());
        assertEquals(330032, body.get(0).getId().intValue());
        assertEquals(330031, body.get(1).getId().intValue());
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
                        .put("order_item_id", 330037)
                        .put("returned_quantity", 1))
                .put(json()
                        .put("order_item_id", 330038)
                        .put("returned_quantity", 2));
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
    	ResponseEntity<Long> response = template.postForEntity("/order/return", request, Long.class);
    	
    	assertEquals(OK, response.getStatusCode());

        Optional<ReturnRequestEntity> entity = returnRequestRepo.findByReturnRequestId(response.getBody());

        checkReturnRequestData(entity);
        assertReturnRequestItemsCreated(body, entity);
    }



    private void checkReturnRequestData(Optional<ReturnRequestEntity> entity) {
        assertTrue(entity.isPresent());
        assertEquals(NEW.getValue() , entity.get().getStatus());
        assertNull(entity.get().getCreatedByEmployee());
        assertEquals(88L, entity.get().getCreatedByUser().getId().longValue());
        assertNotNull(entity.get().getCreatedOn());
        assertEquals(310004L, entity.get().getMetaOrder().getId().longValue());
    }



    private void assertReturnRequestItemsCreated(JSONObject body, Optional<ReturnRequestEntity> entity) {
        Set<ReturnRequestItemEntity> returnedItems = entity.get().getReturnedItems();
        List<Long> ids =
                returnedItems
                        .stream()
                        .map(ReturnRequestItemEntity::getBasket)
                        .map(BasketsEntity::getId)
                        .collect(toList());
        List<Integer> quantities =
                returnedItems
                        .stream()
                        .map(ReturnRequestItemEntity::getReturnedQuantity)
                        .collect(toList());
        ;
        assertEquals(body.getJSONArray("item_list").length(), returnedItems.size());
        assertTrue("expected Returned quantities", asList(1,2).containsAll(quantities));
        assertTrue("expected Basket items ids", asList(330037L, 330038L).containsAll(ids));
    }
}
