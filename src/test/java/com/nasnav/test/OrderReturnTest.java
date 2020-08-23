package com.nasnav.test;

import com.nasnav.NavBox;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.ReturnRequestItemRepository;
import com.nasnav.dao.ReturnRequestRepository;
import com.nasnav.dao.StockRepository;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.assertj.core.util.Arrays.asList;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
public class OrderReturnTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private OrdersRepository orderRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ReturnRequestRepository returnRequestRepo;
    @Autowired
    private ReturnRequestItemRepository returnRequestItemRepo;


    @Test
    public void ReturnOrderItemSuccess() {
        JSONObject returnedItemsList = json()
                .put("return_request_item_id", 10001)
                .put("received_quantity", 1);

        JSONObject body = json().put("returned_items", asList(returnedItemsList));
        HttpEntity request = getHttpEntity(body.toString(), "101112");

        ResponseEntity<String> response = template.postForEntity("/order/return/received_item", request, String.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
    }

}
