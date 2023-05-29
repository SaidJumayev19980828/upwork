package com.nasnav.test;

import com.nasnav.NavBox;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.persistence.PackageRegisteredEntity;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Package_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class PackageTest {

    @Autowired
    private TestRestTemplate template;

    @Test
    public void getListPackageSuccess() {
        ResponseEntity<Object[]> response = template.exchange("/package", GET, null, Object[].class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().length);
    }

    @Test
    public void createPackageTest() {
        String requestBody = json().put("name", "first name ").put("description", "description tes ").put("price", 1.5).toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/package/create", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updatePackageTest() {
        String requestBody = json().put("name", "updated name ").put("description", "description updated ").put("price", 2.5).toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.exchange("/package/" + 100L, PUT, json, Void.class);
        ResponseEntity<Object[]> res = template.exchange("/package", GET, null, Object[].class);
        Map<String, Object> body = (Map<String, Object>) res.getBody()[1];
        String name = (String) body.get("name");
        assertEquals("updated name ", name);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void removePackageTest() {
        HttpEntity<?> json = getHttpEntity("abcdefg");
        ResponseEntity<Object[]> resBefore = template.exchange("/package", GET, null, Object[].class);
        assertEquals(2, resBefore.getBody().length);
        ResponseEntity<Void> response = template.exchange("/package/" + 101L, DELETE, json, Void.class);
        ResponseEntity<Object[]> resAfter = template.exchange("/package", GET, null, Object[].class);
        assertEquals(1, resAfter.getBody().length);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testCompleteRegister() {
        String requestBody = json().put("user_id", 88L).put("package_id", 100L).toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<PackageRegisteredEntity> response = template.postForEntity("/package/complete-profile", json, PackageRegisteredEntity.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(100L,response.getBody().getPackageEntity().getId().longValue());
    }

}
