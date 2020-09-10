package com.nasnav.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.ReturnRequestItemRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.response.ReturnRequestDTO;
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
import java.util.Arrays;
import java.util.List;

import static com.nasnav.enumerations.ReturnRequestStatus.RECEIVED;
import static com.nasnav.test.commons.TestCommons.*;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
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

        List<ReturnRequestItemEntity> items =  returnRequestItemRepo.findByBasket_IdIn(Arrays.asList((330031L)));
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
        assertEquals(2, body.size());
        assertEquals(330032, body.get(0).getId().intValue());
        assertEquals(330031, body.get(1).getId().intValue());
    }
}
