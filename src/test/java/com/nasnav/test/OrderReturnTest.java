package com.nasnav.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.ReturnRequestItemRepository;
import com.nasnav.dao.ReturnRequestRepository;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.nasnav.enumerations.ReturnRequestStatus.RECEIVED;
import static com.nasnav.test.commons.TestCommons.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
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
    private OrdersRepository orderRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ReturnRequestRepository returnRequestRepo;
    @Autowired
    private ReturnRequestItemRepository returnRequestItemRepo;


    @Test
    public void getReturnRequests() throws IOException {
        HttpEntity request = getHttpEntity( "131415");
        ResponseEntity<String> response = template.exchange("/order/return/requests", GET, request, String.class);
        List<ReturnRequestDTO> body = mapper.readValue(response.getBody(), new TypeReference<List<ReturnRequestDTO>>(){});
        assertEquals(200,response.getStatusCodeValue());
        assertEquals(2, body.size());
        assertEquals(330032, body.get(0).getId().intValue());
        assertEquals(330031, body.get(1).getId().intValue());
    }
}
