package com.nasnav.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.ReturnRequestItemRepository;
import com.nasnav.dao.ReturnRequestRepository;
import com.nasnav.dao.ReturnShipmentRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.response.ReturnRequestDTO;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.ReturnRequestEntity;
import com.nasnav.persistence.ReturnRequestItemEntity;
import com.nasnav.response.ReturnRequestsResponse;
import com.nasnav.persistence.ReturnShipmentEntity;
import com.nasnav.service.MailService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.enumerations.ReturnRequestStatus.*;
import static com.nasnav.enumerations.ShippingStatus.REQUSTED;
import static com.nasnav.service.OrderService.ORDER_RETURN_CONFIRM_SUBJECT;
import static com.nasnav.test.commons.TestCommons.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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

    @MockBean
    private MailService mailService;


    @Autowired
    private ReturnShipmentRepository returnShipmentRepo;

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
    public void returnOrderUsingRequestItemsAndBasketItems() {
        JSONArray basketItems = jsonArray().put(json().put("order_item_id", 330034).put("received_quantity", 1));
        JSONArray requestItems = jsonArray().put(json().put("return_request_item_id", 330033).put("received_quantity", 1));

        JSONObject body = 
        		json()
                .put("basket_items", basketItems)
                .put("returned_items", requestItems);
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);

        assertEquals("providing both basket items and returned items will throw an error"
                ,406, response.getStatusCodeValue());
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
        ResponseEntity<ReturnRequestsResponse> response = template.exchange("/order/return/requests", GET, request, ReturnRequestsResponse.class);
        Set<ReturnRequestDTO> body = response.getBody().getReturnRequests();
        Set<Long> ids = getReturnedRequestsIds(body);
        assertEquals(200,response.getStatusCodeValue());
        assertEquals(3, body.size());
        assertTrue( asList(330032L, 330031L, 440034L).containsAll(ids));
    }


    @Test
    public void getReturnRequestsDifferentFilters() throws IOException {
        //count filter
        Set<ReturnRequestDTO> body = getReturnRequests("131415", "count=1");
        assertEquals(1, body.size());
        Set<Long> ids = getReturnedRequestsIds(body);
        assertTrue(setOf(440034L).containsAll(ids));

        //status filter
        body = getReturnRequests("131415", "status=NEW");
        assertEquals(1, body.size());
        ids = getReturnedRequestsIds(body);
        assertTrue(setOf(330031L).containsAll(ids));

        //meta order filter
        body = getReturnRequests("131415", "meta_order_id=310001");
        assertEquals(1, body.size());
        ids = getReturnedRequestsIds(body);
        assertTrue(setOf(330031L).containsAll(ids));

        //shop id filter
        body = getReturnRequests("131415", "shop_id=501");
        assertEquals(1, body.size());
        ids = getReturnedRequestsIds(body);
        assertTrue(setOf(330031L).containsAll(ids));
    }


    private Set<ReturnRequestDTO> getReturnRequests(String authToken, String params) throws IOException {
        HttpEntity<?> request = getHttpEntity( authToken);
        ResponseEntity<ReturnRequestsResponse> response = template.exchange("/order/return/requests?"+params, GET, request, ReturnRequestsResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        return response.getBody().getReturnRequests();
    }


    private Set<Long> getReturnedRequestsIds(Set<ReturnRequestDTO> returnedItems) {
        return returnedItems
                    .stream()
                    .map(ReturnRequestDTO::getId)
                    .collect(toSet());
    }


    @Test
    public void getReturnRequestsInvalidAuthZ() {
        HttpEntity<?> request = getHttpEntity( "101112");
        ResponseEntity<String> response = template.exchange("/order/return/requests", GET, request, String.class);
        assertEquals(403,response.getStatusCodeValue());
    }


    @Test
    public void getReturnRequestsInvalidAuthN() {
        HttpEntity<?> request = getHttpEntity( "invalid token");
        ResponseEntity<String> response = template.exchange("/order/return/requests", GET, request, String.class);
        assertEquals(401,response.getStatusCodeValue());
    }


    @Test
    public void getReturnRequest() {
        HttpEntity<?> request = getHttpEntity( "131415");
        ResponseEntity<ReturnRequestDTO> response = template.exchange("/order/return/request?id=330031", GET, request, ReturnRequestDTO.class);

        assertEquals(200,response.getStatusCodeValue());
        ReturnRequestDTO body = response.getBody();
        assertEquals(330031, body.getId().longValue());
        assertEquals(310001, body.getMetaOrderId().longValue());
        assertFalse(body.getReturnedItems().isEmpty());
    }


    @Test
    public void getReturnRequestAnotherOrg() {
        HttpEntity<?> request = getHttpEntity( "131415");
        ResponseEntity<ReturnRequestDTO> response = template.exchange("/order/return/request?id=330033", GET, request, ReturnRequestDTO.class);

        assertEquals(406,response.getStatusCodeValue());
    }


    @Test
    public void getReturnRequestInvalidAuthZ() {
        HttpEntity<?> request = getHttpEntity( "101112");
        ResponseEntity<String> response = template.exchange("/order/return/request?id=330031", GET, request, String.class);
        assertEquals(403,response.getStatusCodeValue());
    }


    @Test
    public void getReturnRequestInvalidAuthN() {
        HttpEntity<?> request = getHttpEntity( "invalid token");
        ResponseEntity<String> response = template.exchange("/order/return/request?id=330031", GET, request, String.class);
        assertEquals(401,response.getStatusCodeValue());
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
        return json().put("item_list", returnedItems);
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
        return json().put("item_list", returnedItems);
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
        return json().put("item_list", returnedItems);
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
        return json().put("item_list", returnedItems);
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
        return json().put("item_list", returnedItems);
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
        return json().put("item_list", returnedItems);
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
        return json().put("item_list", returnedItems);
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
        return json().put("item_list", returnedItems);
	}
    
    
    
    
    @Test
    public void customerCreateReturnOrderTest() {
    	JSONObject body = createReturnRequestBody();
		
    	HttpEntity<?> request = getHttpEntity(body.toString(), "123");
    	ResponseEntity<Long> response = template.postForEntity("/order/return", request, Long.class);
    	
    	assertEquals(OK, response.getStatusCode());

        Optional<ReturnRequestEntity> entity = returnRequestRepo.findByReturnRequestId(response.getBody(), 99001L);

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
        assertEquals(body.getJSONArray("item_list").length(), returnedItems.size());
        assertTrue("expected Returned quantities", asList(1,2).containsAll(quantities));
        assertTrue("expected Basket items ids", asList(330037L, 330038L).containsAll(ids));
    }


    @Test
    public void rejectReturnOrderRequest() {
        JSONObject body = json().put("return_request_id", 330031)
                .put("rejection_reason", "damaged product");
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> res = template.postForEntity("/order/return/reject", request, String.class);
        assertEquals(200, res.getStatusCodeValue());

        Optional<ReturnRequestEntity> entity = returnRequestRepo.findByIdAndOrganizationIdAndStatus(330031L, 99001L, REJECTED.getValue());
        assertTrue(entity.isPresent());
    }


    @Test
    public void rejectReturnOrderRequestConfirmedRequest() {
        JSONObject body = json().put("return_request_id", 330032)
                .put("rejection_reason", "damaged product");
        HttpEntity<?> request = getHttpEntity(body.toString(), "131415");

        ResponseEntity<String> res = template.postForEntity("/order/return/reject", request, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }




    @Test
    public void confirmReturnRequestAuthNTest(){
        Long id= 450002L;
        HttpEntity<?> request = getHttpEntity("INVALID");

        ResponseEntity<String> res = template.postForEntity("/order/return/confirm?id="+id, request, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }





    @Test
    public void confirmReturnRequestAuthZTest(){
        Long id= 450001L;
        HttpEntity<?> request = getHttpEntity("101112");

        ResponseEntity<String> res = template.postForEntity("/order/return/confirm?id="+id, request, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }



    @Test
    public void confirmReturnRequestNonExistingRequestTest(){
        Long id= -1L;
        HttpEntity<?> request = getHttpEntity("131415");

        ResponseEntity<String> res = template.postForEntity("/order/return/confirm?id="+id, request, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }



    @Test
    public void confirmReturnRequestFromAnotherOrgTest(){
        Long id= -1L;
        HttpEntity<?> request = getHttpEntity("131415");

        ResponseEntity<String> res = template.postForEntity("/order/return/confirm?id="+id, request, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }



    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_10.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void confirmReturnRequestWithInvalidStateTest(){
        Long id= 450002L;
        HttpEntity<?> request = getHttpEntity("131415");

        ResponseEntity<String> res = template.postForEntity("/order/return/confirm?id="+id, request, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }




    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_10.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void confirmReturnRequestTest() throws MessagingException, InterruptedException {
        Long id= 450001L;
        ReturnRequestEntity entityBefore = returnRequestRepo.findById(id).get();
        assertEquals(NEW.getValue(), entityBefore.getStatus());
        //-----------------------------------------------

        HttpEntity<?> request = getHttpEntity("131415");

        ResponseEntity<String> res = template.postForEntity("/order/return/confirm?id="+id, request, String.class);
        Thread.sleep(1000);
        //-----------------------------------------------
        assertReturnShipmentsCreated(id, res);

        Mockito
            .verify(mailService)
            .sendThymeleafTemplateMail(
                    Mockito.eq("user1@nasnav.com")
                    , Mockito.eq(ORDER_RETURN_CONFIRM_SUBJECT)
                    , Mockito.anyString()
                    , Mockito.anyMap()
                    , Mockito.anyList());
    }



    private void assertReturnShipmentsCreated(Long id, ResponseEntity<String> res) {
        assertEquals(200, res.getStatusCodeValue());

        ReturnRequestEntity entity = returnRequestRepo.findById(id).get();
        assertEquals(CONFIRMED.getValue(), entity.getStatus());

        List<ReturnShipmentEntity> shipments = returnShipmentRepo.findByReturnRequest_Id(id);
        assertEquals("each item was from different shop, so each will have " +
                "separate shipment , the same as how they are shipped to customer"
                ,2, shipments.size());

        assertTrue("each item was from different shop, so each will have " +
                        "separate shipment , the same as how they are shipped to customer"
                ,isAllHasSingleItem(shipments));
        assertTrue(isAllHasRequestedStatus(shipments));
        assertTrue( isAllHasTrackingData(shipments));
    }



    private boolean isAllHasSingleItem(List<ReturnShipmentEntity> shipments) {
        return shipments
                .stream()
                .map(ReturnShipmentEntity::getReturnRequestItems)
                .allMatch(itms -> itms.size() == 1);
    }



    private boolean isAllHasRequestedStatus(List<ReturnShipmentEntity> shipments) {
        return shipments
                .stream()
                .allMatch(shp -> REQUSTED.getValue().equals(shp.getStatus()));
    }



    private boolean isAllHasTrackingData(List<ReturnShipmentEntity> shipments) {
        return shipments
                .stream()
                .allMatch(shp -> noneIsNull(shp.getExternalId(), shp.getTrackNumber(), shp.getShippingServiceId()));
    }
}
