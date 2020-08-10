package com.nasnav.test;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.EntityUtils.DEFAULT_TIMESTAMP_PATTERN;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.PromotionRepository;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.persistence.PromotionsEntity;

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
	
	@Autowired
	private PromotionRepository promoRepo;
	
	
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_TIMESTAMP_PATTERN);
	
	
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
		String url = format("/organization/promotions?status=ACTIVE&start=%s&end=%s",now, now);
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
	public void getPromotionsWithStatusFilterTest() throws Exception{
		HttpEntity<?> req = getHttpEntity("hijkllm");
		String now = formatter.format(now());
		String url = format("/organization/promotions?status=INACTIVE",now, now);
        ResponseEntity<String> res = 
        		template.exchange(url, GET, req, String.class);
        
        assertEquals(200, res.getStatusCodeValue());
        List<PromotionDTO> promotions = objectMapper.readValue(res.getBody(), new TypeReference<List<PromotionDTO>>() {});
        assertEquals(1, promotions.size());
        Set<Long> expectedIds = setOf(630004L);
        Set<Long> ids = promotions.stream().map(PromotionDTO::getId).collect(toSet());
        assertEquals(expectedIds, ids);
	} 
	
	
	
	
	@Test
	public void getPromotionsWithGivenIdFilterTest() throws Exception{
		HttpEntity<?> req = getHttpEntity("hijkllm");
		String now = formatter.format(now());
		String url = format("/organization/promotions?id=630001",now, now);
        ResponseEntity<String> res = 
        		template.exchange(url, GET, req, String.class);
        
        assertEquals(200, res.getStatusCodeValue());
        List<PromotionDTO> promotions = objectMapper.readValue(res.getBody(), new TypeReference<List<PromotionDTO>>() {});
        assertEquals(1, promotions.size());
        Set<Long> expectedIds = setOf(630001L);
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
	
	
	
	
	
	@Test
	public void createPromotionsMissingParamsTest() {
		JSONObject bodyJson = createPromotionRequest();
		bodyJson.remove("code");
		String body = bodyJson.toString();
		
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotion", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
	}
	
	
	
	
	@Test
	public void createPromotionsCodeAlreadyInUseTest() {
		JSONObject bodyJson = createPromotionRequest();
		bodyJson.put("code", "MONEY2020");
		String body = bodyJson.toString();
		
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotion", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
	}
	
	
	
	
	@Test
	public void createPromotionsWithCodeUsedByOldPromoTest() {
		JSONObject bodyJson = createPromotionRequest();
		bodyJson.put("code", "MORE2020");
		String body = bodyJson.toString();
		
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotion", POST, req, String.class);
        assertEquals("Old codes can be reused", 200, res.getStatusCodeValue());
	}
	
	
	
	
	
	@Test
	public void createPromotionsInvalidDatesTest() {String end = formatter.format(now().minusDays(2));
		String start = formatter.format(now().plusDays(3));
		JSONObject bodyJson = createPromotionRequest();
		bodyJson
		.put("start_date", start)
		.put("end_date", end);
		String body = bodyJson.toString();
		
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotion", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
	}
	
	
	
	
	
	@Test
	public void createPromotionsInvalidJsonTest() {
		JSONObject bodyJson = createPromotionRequest();
		bodyJson.put("constrains", "{");
		String body = bodyJson.toString();
		
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotion", POST, req, String.class);
        assertEquals(400, res.getStatusCodeValue());
	}
	
	
	
	
	
	@Test
	public void createPromotionsInvalidStatusTest() {
		JSONObject bodyJson = createPromotionRequest();
		bodyJson
		.put("status", "NOT VALID");
		String body = bodyJson.toString();
		
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotion", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
	}
	
	
	
	
	
	
	@Test
	public void createPromotionsInvalidStatusForUpdateTest() {
		JSONObject bodyJson = createPromotionRequest();
		bodyJson.put("id", 630001L);
		
		String body = bodyJson.toString();
		
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/promotion", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
	}
	
	
	
	
	
	@Test
	public void createPromotionsSuccessTest() {
		JSONObject bodyJson = createPromotionRequest();
		String body = bodyJson.toString();
		
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        ResponseEntity<Long> res = 
        		template.exchange("/organization/promotion", POST, req, Long.class);
        assertEquals(200, res.getStatusCodeValue());
        
        assertPromoUpdated(bodyJson, res);
	}
	
	
	
	
	@Test
	public void updatePromotionsSuccessTest() {
		JSONObject bodyJson = createPromotionRequest();
		bodyJson.put("id", 630004L);
		String body = bodyJson.toString();
		
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        ResponseEntity<Long> res = 
        		template.exchange("/organization/promotion", POST, req, Long.class);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals(630004L, res.getBody().longValue());
        assertPromoUpdated(bodyJson, res);
	}
	
	
	
	
	@Test
	public void gerPromotionDiscountTest() {
		JSONObject bodyJson = createPromotionRequest();
		bodyJson.put("id", 630004L);
		String body = bodyJson.toString();
		
		HttpEntity<?> req = getHttpEntity(body, "123");
        ResponseEntity<BigDecimal> res = 
        		template.exchange("/cart/promo/discount?promo=GREEEEEEEEED", GET, req, BigDecimal.class);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals(0, res.getBody().compareTo(new BigDecimal("310")));
	}



	private void assertPromoUpdated(JSONObject bodyJson, ResponseEntity<Long> res) {
		PromotionsEntity entity = 
        		promoRepo
        		.findById(res.getBody())
        		.orElseThrow(() -> new IllegalStateException());
        
        JSONObject savedConstrainJson = new JSONObject(entity.getConstrainsJson());
        JSONObject savedDiscountJson = new JSONObject(entity.getDiscountJson());
        
        assertTrue(savedConstrainJson.similar(bodyJson.get("constrains")));
        assertTrue(savedDiscountJson.similar(bodyJson.get("discount")));
        assertNotNull(entity.getCreatedOn());
        assertNotNull(entity.getDateEnd());
        assertNotNull(entity.getDateStart());
        assertEquals(1, entity.getStatus().intValue());
        assertEquals(bodyJson.get("identifier"), entity.getIdentifier());
        assertEquals(bodyJson.get("code"), entity.getCode());
	}



	
	
	private JSONObject createPromotionRequest() {
		String start = formatter.format(now().plusDays(2));
		String end = formatter.format(now().plusDays(3));
		return json()
				.put("identifier", "awsome-promo")
				.put("start_date", start)
				.put("end_date", end)
				.put("status", "ACTIVE")
				.put("code", "GIVE-YOUR-MONEY-OR-ELSE-...")
				.put("constrains", json().put("amount_max", 1000))
				.put("discount", json().put("percentage", 20));
	}
}
