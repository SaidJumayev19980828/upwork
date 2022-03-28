package com.nasnav.yeshtery.test.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.ShopThreeSixtyDTO;
import com.nasnav.dto.response.ProductsPositionDTO;
import com.nasnav.yeshtery.Yeshtery;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class ThreeSixtyControllerTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void get360JsonData() {
        var response = template.getForEntity("/v1/360view/json_data?shop_id=501&type=web", String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("{}", response.getBody());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void get360Setions() throws JsonProcessingException {
        var response = template.getForEntity("/v1/yeshtery/360view/sections?shop_id=501", String.class);
        assertEquals(200, response.getStatusCodeValue());
        Map body = mapper.readValue(response.getBody(), new TypeReference<Map>() {
        });
        assertEquals(1, body.size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void get360Shop() {
        var response = template.getForEntity("/v1/360view/shops?shop_id=501", ShopThreeSixtyDTO.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(10010, response.getBody().getId().intValue());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void get360ProductPositions() {
        var response = template.getForEntity("/v1/360view/products_positions?shop_id=501&published=1", ProductsPositionDTO.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getProductsData().size());
        assertEquals(1, response.getBody().getCollectionsData().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void get360Products() {
        var response = template.getForEntity("/v1/360view/products?shop_id=501&published=1", LinkedHashMap.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(3, ((List) response.getBody().get("products")).size());
    }
}
