package com.nasnav.controller;

import com.nasnav.NavBox;
import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.response.ApiResponse;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.OrderService;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.BodyInserters;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
//@PropertySource("classpath:database.properties")
public class OrdersControllerTest {


    @Mock
    private OrdersController ordersController;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    OrderService orderService;
    
    //@Test
    public void updateOrderUnauthorizedUserTest() {
    	HttpHeaders headers = new HttpHeaders();
    	  headers.set("user-id", "123");
    	  headers.set("user-token", "token");
    	  //headers.set("content-type", "applicantion/json");
    	  
    	  HttpEntity<Object> orderJson = new HttpEntity<>(
    			  "{\t\n" +
    		                "\t\"status\":\"5\",\n" +
    		                "\t\"basket\":\"basket\"\n" +
    		                "\t\"address\":\"address\"" + 
    		                "}", headers);
          ResponseEntity<OrderResponse> response = template.postForEntity(
                  "/order/update", orderJson, OrderResponse.class);
    	  
          assertEquals(401, response.getStatusCode().value());         
    }
    
    //@Test
    public void updateOrderNewStatusEmptyBasketTest() {
  	  
  	  HttpHeaders headers = new HttpHeaders();
  	  headers.set("user-id", "77");
  	  headers.set("user-token", "token");
  	  
  	  HttpEntity<Object> orderJson = new HttpEntity<>(
  			"{\t\n" +
  	                "\t\"status\":\"0\",\n" +
  	                "\t\"basket\":\"\"\n" +
  	                "\t\"address\":\"address\"" + 
  	                "}", headers);
        ResponseEntity<OrderResponse> response = template.postForEntity(
                "/order/update", orderJson, OrderResponse.class);
  	  
        assertEquals(406, response.getStatusCode().value());
    }

    
}
