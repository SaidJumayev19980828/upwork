package com.nasnav.test;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dto.response.PromotionDTO;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert.sql"})
@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
public class PromotionsTest {
	@Autowired
    private TestRestTemplate template;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyyThh:mm");
	
	
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
	public void getPromotionsWithFiltersTest() throws Exception{
		HttpEntity<?> req = getHttpEntity("hijkllm");
		String now = formatter.format(now());
		String url = String.format("/organization/promotions?status=active&start=%s&end=%s",now, now);
        ResponseEntity<String> res = 
        		template.exchange(url, GET, req, String.class);
        
        assertEquals(200, res.getStatusCodeValue());
        List<PromotionDTO> promotions = objectMapper.readValue(res.getBody(), new TypeReference<List<PromotionDTO>>() {});
        assertEquals(2, promotions.size());
        Set<Long> expectedIds = setOf(630002L,630003L);
        Set<Long> ids = promotions.stream().map(PromotionDTO::getId).collect(toSet());
        assertEquals(expectedIds, ids);
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
