package com.nasnav.test;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

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

import com.nasnav.NavBox;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert.sql"})
@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
public class PromotionsTest {
	@Autowired
    private TestRestTemplate template;
	
	
	
	@Test
	public void getPromotionsAuthZTest() {
		HttpEntity<?> req = getHttpEntity("123456");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotions", GET, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
	}
	
	
	
	@Test
	public void getPromotionsAuthNTest() {
		HttpEntity<?> req = getHttpEntity("NOT EXIST");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotions", GET, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
	}
	
	
	
	
	
	@Test
	public void getPromotionAuthZTest() {
		HttpEntity<?> req = getHttpEntity("123456");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotion", GET, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
	}
	
	
	
	
	
	@Test
	public void getPromotionAuthNTest() {
		HttpEntity<?> req = getHttpEntity("NOT EXIST");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotion", GET, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
	}
	
	
	
	
	
	@Test
	public void createPromotionsAuthZTest() {
		HttpEntity<?> req = getHttpEntity("123456");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotion", POST, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
	}
	
	
	
	@Test
	public void createPromotionsAuthNTest() {
		HttpEntity<?> req = getHttpEntity("NOT EXIST");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotion", POST, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
	}
}
