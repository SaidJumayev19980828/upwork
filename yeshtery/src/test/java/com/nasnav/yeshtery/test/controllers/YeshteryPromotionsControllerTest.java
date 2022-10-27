package com.nasnav.yeshtery.test.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.YeshteryConstants;
import com.nasnav.service.ProductsPromotionsDTO;
import com.nasnav.yeshtery.Yeshtery;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Get_List_Promotions_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class YeshteryPromotionsControllerTest {

    private final String YESHTERY_PROMOTIONS_LIST_API_PATH = YeshteryConstants.API_PATH + "/yeshtery/promotions_list";
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getPromotionsList() throws JsonProcessingException {
        ResponseEntity<String> response = template.getForEntity(YESHTERY_PROMOTIONS_LIST_API_PATH+"?ids=1001,1002,1003,1004,1005", String.class);
        List<ProductsPromotionsDTO> body = mapper.readValue(response.getBody(), new TypeReference<List<ProductsPromotionsDTO>>() {});

        assertEquals(4, body.size());
    }
}