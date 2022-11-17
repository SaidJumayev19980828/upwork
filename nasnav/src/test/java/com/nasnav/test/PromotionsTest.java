package com.nasnav.test;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.EntityUtils.DEFAULT_TIMESTAMP_PATTERN;
import static com.nasnav.enumerations.PromotionStatus.TERMINATED;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.lang.String.format;
import static java.math.BigDecimal.ROUND_HALF_EVEN;
import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.nasnav.dto.AppliedPromotionsResponse;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.dto.response.PromotionResponse;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.Order;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.PromotionRepository;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.persistence.PromotionsEntity;
import com.nasnav.service.ItemsPromotionsDTO;

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
	public void getPromotionsAllAttrTest(){
		JSONObject bodyJson = createPromotionRequest();
		String body = bodyJson.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");

		ResponseEntity<Long> postRes =
				template.exchange("/organization/promotion", POST, req, Long.class);

		PromotionsEntity promotion = promoRepo.findById(postRes.getBody()).get();

		assertNotNull(promotion.getId());
		assertNotNull(promotion.getDateStart());
		assertNotNull(promotion.getDateEnd());
		assertNotNull(promotion.getConstrainsJson());
		assertNotNull(promotion.getDiscountJson());
		assertEquals(promotion.getIdentifier(), "awsome-promo");
		assertEquals(promotion.getName(), "promo_name");
		assertEquals(promotion.getDescription(), "promo_desc");
		assertEquals(promotion.getBanner(), "promo_banner");
		assertEquals(promotion.getCover(), "promo_cover");
		assertEquals(promotion.getStatus(), Integer.valueOf(1));
		assertEquals(promotion.getCode(), "GIVE-YOUR-MONEY-OR-ELSE-...");
		assertEquals(promotion.getTypeId(), Integer.valueOf(0));


	}

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
		String url = format("/organization/promotions?status=ACTIVE&start_date=%s&end_date=%s",now, now);
        ResponseEntity<PromotionResponse> res =
        		template.exchange(url, GET, req, PromotionResponse.class);
        
        assertEquals(200, res.getStatusCodeValue());
        List<PromotionDTO> promotions = res.getBody().getPromotions();
        assertEquals(2, promotions.size());
        Set<Long> expectedIds = setOf(630002L,630003L);
        Set<Long> ids = promotions.stream().map(PromotionDTO::getId).collect(toSet());
        assertEquals(expectedIds, ids);
	}


	@Test
	public void getPromotionStartAndCountTest() {
		HttpEntity<?> req = getHttpEntity("hijkllm");

		ResponseEntity<PromotionResponse> res =
				template.exchange("/organization/promotions?count=1", GET, req, PromotionResponse.class);

		assertEquals(200, res.getStatusCodeValue());
		List<PromotionDTO> promotions = res.getBody().getPromotions();
		assertEquals(5, res.getBody().getTotal().intValue());
		assertEquals(1, promotions.size());

		res = template.exchange("/organization/promotions?start=4", GET, req, PromotionResponse.class);

		assertEquals(200, res.getStatusCodeValue());
		promotions = res.getBody().getPromotions();
		assertEquals(630001, promotions.get(0).getId().intValue());
	}


	@Test
	public void getPromotionsWithStatusFilterTest() throws Exception{
		HttpEntity<?> req = getHttpEntity("hijkllm");
		String now = formatter.format(now());
		String url = format("/organization/promotions?status=INACTIVE",now, now);
		ResponseEntity<PromotionResponse> res =
				template.exchange(url, GET, req, PromotionResponse.class);
        
        assertEquals(200, res.getStatusCodeValue());
        List<PromotionDTO> promotions = res.getBody().getPromotions();
        assertEquals(2, promotions.size());
        Set<Long> expectedIds = setOf(630004L, 630005L);
        Set<Long> ids = promotions.stream().map(PromotionDTO::getId).collect(toSet());
        assertEquals(expectedIds, ids);
	}


	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_3.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getPromotionsWithInvalidDataTest() throws Exception{
		HttpEntity<?> req = getHttpEntity("hijkllm");
		String now = formatter.format(now());
		String url = format("/organization/promotions",now, now);
		ResponseEntity<PromotionResponse> res =
				template.exchange(url, GET, req, PromotionResponse.class);

		assertEquals(200, res.getStatusCodeValue());
		List<PromotionDTO> promotions = res.getBody().getPromotions();
		assertEquals(3, promotions.size());
		Set<Long> expectedIds = setOf(630001L, 630002L, 630003L);
		Set<Long> ids = promotions.stream().map(PromotionDTO::getId).collect(toSet());
		assertEquals(expectedIds, ids);
	}


	@Test
	public void getPromotionsWithGivenIdFilterTest() throws Exception{
		HttpEntity<?> req = getHttpEntity("hijkllm");
		String now = formatter.format(now());
		String url = format("/organization/promotions?id=630001",now, now);
		ResponseEntity<PromotionResponse> res =
				template.exchange(url, GET, req, PromotionResponse.class);
        
        assertEquals(200, res.getStatusCodeValue());
        List<PromotionDTO> promotions = res.getBody().getPromotions();
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
	public void createPromotionsCodeAlreadyInUseButWithOtherCaseTest() {
		JSONObject bodyJson = createPromotionRequest();
		bodyJson.put("code", "monEy2020");
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
	public void createPromotionsUsingZonedDateTimeTest() {
		JSONObject bodyJson = createPromotionRequest();
		LocalDateTime startDate = now().withNano(0).plusDays(2);
		ZoneId utcZone = ZoneId.of("UTC");
		ZonedDateTime startDateWithZone = startDate.atZone(utcZone);
		bodyJson.put("start_date", startDateWithZone.format(formatter));
		String body = bodyJson.toString();

		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
		ResponseEntity<Long> res =
				template.exchange("/organization/promotion", POST, req, Long.class);
		assertEquals(200, res.getStatusCodeValue());
		LocalDateTime entityStartDate = promoRepo.findById(res.getBody()).get().getDateStart();
		assertEquals(startDate, entityStartDate);
		assertPromoUpdated(bodyJson, res);
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
	public void getPromotionDiscountTest() {
		String promoCode = "GREEEEEED";
		var discount = getCartDiscount(promoCode);
        assertEquals(0, new BigDecimal("270").compareTo(discount));
	}



	private BigDecimal getCartDiscount(String promoCode) {
		String url = isNull(promoCode)?
						"/cart/promo/discount": format("/cart/promo/discount?promo=%s", promoCode);
		HttpEntity<?> req = getHttpEntity("123");
		ResponseEntity<AppliedPromotionsResponse> res =
				template.exchange(url, GET, req, AppliedPromotionsResponse.class);
		assertEquals(200, res.getStatusCodeValue());
		System.out.println(res.getBody().toString());
		return res.getBody().getTotalDiscount();
	}


	@Test
	public void getPromotionDiscountNoExistingPromoTest() {
		String promoCode = "NotExist";
		String url = format("/cart/promo/discount?promo=%s", promoCode);
		HttpEntity<?> req = getHttpEntity("123");
		ResponseEntity<String> res = 
        		template.exchange(url, GET, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
	}
	
	
	
	
	@Test
	public void getPromotionDiscountExpiredPromoTest() {
		String promoCode = "MORE2020";
		String url = format("/cart/promo/discount?promo=%s", promoCode);
		HttpEntity<?> req = getHttpEntity("123");
		ResponseEntity<String> res = 
        		template.exchange(url, GET, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
	}
	
	
	
	
	@Test
	public void getPromotionDiscountNotApplicablePromoTest() {
		String promoCode = "MONEY2020";
		String url = format("/cart/promo/discount?promo=%s", promoCode);
		HttpEntity<?> req = getHttpEntity("123");
        ResponseEntity<String> res = 
        		template.exchange(url, GET, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
	}
	
	
	
	
	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_2.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getPromotionDiscountAlreadyUsedTest() {
		String promoCode = "GREEEEEED";
		String url = format("/cart/promo/discount?promo=%s", promoCode);
		HttpEntity<?> req = getHttpEntity("123");
		ResponseEntity<String> res = 
        		template.exchange(url, GET, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
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
				.put("name", "promo_name")
				.put("description", "promo_desc")
				.put("banner", "promo_banner")
				.put("cover", "promo_cover")
				.put("start_date", start)
				.put("end_date", end)
				.put("status", "ACTIVE")
				.put("code", "GIVE-YOUR-MONEY-OR-ELSE-...")
				.put("constrains", json()
						.put("discount_value_max", 1000)
						.put("cart_amount_min", 1))
				.put("discount", json().put("percentage", 20))
				.put("type_id", 0);
	}



	@Test
	public void deletePromotionAuthZTest() {
		HttpEntity<?> req = getHttpEntity("123456");
		ResponseEntity<String> res =
				template.exchange("/organization/promotion?id=630001", DELETE, req, String.class);
		assertEquals(403, res.getStatusCodeValue());
	}



	@Test
	public void deletePromotionAuthNTest() {
		HttpEntity<?> req = getHttpEntity("NOT EXIST");
		ResponseEntity<String> res =
				template.exchange("/organization/promotion?id=630001", DELETE, req, String.class);
		assertEquals(401, res.getStatusCodeValue());
	}



	@Test
	public void deletePromotionOfOtherOrgAuthNTest() {
		Long id = 630001L;
		Optional<PromotionsEntity> promo = promoRepo.findById(id);
		assertTrue(promo.isPresent());

		//---------------------------------------
		HttpEntity<?> req = getHttpEntity("rtrtyy");
		ResponseEntity<String> res =
				template.exchange("/organization/promotion?id="+id, DELETE, req, String.class);
		assertEquals(406, res.getStatusCodeValue());

		//---------------------------------------
		promo = promoRepo.findById(id);
		assertTrue(promo.isPresent());
	}



	@Test
	public void deletePromotionNewTest() {
		Long id = 630005L;
		Optional<PromotionsEntity> promo = promoRepo.findById(id);
		assertTrue(promo.isPresent());

		//---------------------------------------
		HttpEntity<?> req = getHttpEntity("hijkllm");
		ResponseEntity<String> res =
				template.exchange("/organization/promotion?id="+id, DELETE, req, String.class);
		assertEquals(200, res.getStatusCodeValue());

		//---------------------------------------
		promo = promoRepo.findById(id);
		assertTrue(!promo.isPresent());
	}



	@Test
	public void deletePromotionActivatedTest() {
		Long id = 630001L;
		Optional<PromotionsEntity> promo = promoRepo.findById(id);
		assertTrue(promo.isPresent());
		boolean isEndDateInFuture = promo.get().getDateEnd().isAfter(now());
		assertTrue(isEndDateInFuture);
		assertNotEquals(promo.get().getDateEnd(), promo.get().getDateStart());
		//---------------------------------------
		HttpEntity<?> req = getHttpEntity("hijkllm");
		ResponseEntity<String> res =
				template.exchange("/organization/promotion?id="+id, DELETE, req, String.class);
		assertEquals(200, res.getStatusCodeValue());

		//---------------------------------------
		promo = promoRepo.findById(id);
		assertTrue(promo.isPresent());
		assertEquals("if the promo start date in future, its end date will be set as the start date"
				, promo.get().getDateEnd(), promo.get().getDateStart());
		assertTrue(Objects.equals(TERMINATED.getValue(), promo.get().getStatus()));
	}



	@Test
	public void deletePromotionActivatedAndStartedTest() {
		Long id = 630002L;
		Optional<PromotionsEntity> promo = promoRepo.findById(id);
		assertTrue(promo.isPresent());
		boolean isEndDateInFuture = promo.get().getDateEnd().isAfter(now());
		assertTrue(isEndDateInFuture);
		assertNotEquals(promo.get().getDateEnd(), promo.get().getDateStart());
		//---------------------------------------
		HttpEntity<?> req = getHttpEntity("hijkllm");
		ResponseEntity<String> res =
				template.exchange("/organization/promotion?id="+id, DELETE, req, String.class);
		assertEquals(200, res.getStatusCodeValue());

		//---------------------------------------
		promo = promoRepo.findById(id);
		assertTrue(promo.isPresent());
		isEndDateInFuture = promo.get().getDateEnd().isAfter(now());
		assertFalse("if the promo start date in past, its end date will be now",isEndDateInFuture);
	}


	@Test
	public void deletePromotionTerminatedTest() {
		Long id = 630001L;
		// terminate the promotion first
		HttpEntity<?> req = getHttpEntity("hijkllm");
		ResponseEntity<String> res =
				template.exchange("/organization/promotion?id="+id, DELETE, req, String.class);
		assertEquals(200, res.getStatusCodeValue());
		//reterminate the promotion
		res = template.exchange("/organization/promotion?id="+id, DELETE, req, String.class);
		assertEquals(406, res.getStatusCodeValue());
		assertTrue( res.getBody().contains("PROMO$PARAM$0011"));
	}

	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_4.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getShippingOfferWithShippingPromo() throws IOException {
		HttpEntity<?> req = getHttpEntity("123");
		createCartForUser(req, 601L, 3);
		ResponseEntity<String> res =
				template.exchange("/shipping/offers?customer_address=12300001", GET, req, String.class);
		assertEquals(200, res.getStatusCodeValue());

		List<ShippingOfferDTO> offers = objectMapper.readValue(res.getBody(), new TypeReference<List<ShippingOfferDTO>>(){});
		//actual shipping value is 25.5 and discount is 75% = 19.125 then total shipping 6.375 -> 6.38
		BigDecimal expectedTotalValue = new BigDecimal(6.38).setScale(2, ROUND_HALF_EVEN);
		assertEquals(expectedTotalValue, offers.get(0).getTotal());
	}

    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_4.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getShippingOfferWithLowerShippingPromo() throws IOException {
        HttpEntity<?> req = getHttpEntity("123");
        createCartForUser(req, 601L, 1);
        ResponseEntity<String> res =
                template.exchange("/shipping/offers?customer_address=12300001", GET, req, String.class);
        assertEquals(200, res.getStatusCodeValue());

        List<ShippingOfferDTO> offers = objectMapper.readValue(res.getBody(), new TypeReference<List<ShippingOfferDTO>>(){});
        //actual shipping value is 25.5 and discount is 50% = 12.75 then total shipping 12.75
        BigDecimal expectedTotalValue = new BigDecimal(12.75).setScale(2, ROUND_HALF_EVEN);
        assertEquals(expectedTotalValue, offers.get(0).getTotal());
    }

	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_4.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getCartWithBuyXGetYPromo() {
		HttpEntity<?> req = getHttpEntity("123");
		createCartForUser(req, 601L, 3);
		Cart res = createCartForUser(req, 601L, 3);
		var discount = getCartDiscount(null);

		assertEquals(100, discount.intValue());
	}


	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_4.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getCartWithTotalCartValuePromo() {
		HttpEntity<?> req = getHttpEntity("123");
		Cart res = createCartForUser(req, 602L, 8);
		var discount = getCartDiscount(null);

		assertEquals(80, discount.intValue());
	}

	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_4.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getCartWithTotalCartQuantitiesPromo() {
		HttpEntity<?> req = getHttpEntity("123");
		createCartForUser(req, 602L, 1);
		createCartForUser(req, 603L, 8);
		Cart res = createCartForUser(req, 604L, 1);
		var discount = getCartDiscount(null);
		assertEquals(100, discount.intValue());
	}


	private Cart createCartForUser(HttpEntity<?> req, Long stockId, Integer quantity) {
		String body = json()
			.put("stock_id", stockId)
			.put("quantity", quantity)
			.toString();
		req = new HttpEntity<>(body, req.getHeaders());
		ResponseEntity<Cart> res = template.postForEntity("/cart/item",  req, Cart.class);
		assertEquals(200, res.getStatusCodeValue());
		return res.getBody();
	}


	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_4.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderWithShippingPromo() throws IOException {
		HttpEntity<?> req = getHttpEntity("123");
		createCartForUser(req, 601L, 1);
		String requestBody = createCheckoutDTO().toString();
		req = new HttpEntity<>(requestBody, req.getHeaders());
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", req, Order.class);
		assertEquals(200, res.getStatusCodeValue());
		Order order = res.getBody();

		BigDecimal expectedTotal = new BigDecimal(112.75).setScale(2, ROUND_HALF_EVEN);
		assertEquals(expectedTotal, order.getTotal());
		BigDecimal expectedSubtotal = new BigDecimal(100).setScale(2, ROUND_HALF_EVEN);
		assertEquals(expectedSubtotal, order.getSubtotal());
		BigDecimal expectedShipping = new BigDecimal(12.75).setScale(2, ROUND_HALF_EVEN);
		assertEquals(expectedShipping, order.getShipping());
	}

	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_4.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderWithBuyXGetYPromo() {
		HttpEntity<?> req = getHttpEntity("123");
		createCartForUser(req, 601L, 3);
		String requestBody = createCheckoutDTO().toString();
		req = new HttpEntity<>(requestBody, req.getHeaders());
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", req, Order.class);
		assertEquals(200, res.getStatusCodeValue());
		Order order = res.getBody();

		assertTrue(100 == order.getDiscount().intValue());
		assertTrue(300 == order.getSubtotal().intValue());
		assertTrue(6.38 == order.getShipping().doubleValue());
		assertTrue("total is subTotal - discount + shipping", 206.38 == order.getTotal().doubleValue());
	}

	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_4.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderWithTotalCartValuePromo() {
		HttpEntity<?> req = getHttpEntity("123");
		createCartForUser(req, 602L, 8);
		String requestBody = createCheckoutDTO().toString();
		req = new HttpEntity<>(requestBody, req.getHeaders());
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", req, Order.class);
		assertEquals(200, res.getStatusCodeValue());
		Order order = res.getBody();

		assertTrue(80 == order.getDiscount().intValue());
		assertTrue(800 == order.getSubtotal().intValue());
		assertTrue(6.38 == order.getShipping().doubleValue());
		assertTrue("total is subTotal - discount + shipping", 726.38 == order.getTotal().doubleValue());
	}

	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_4.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderWithTotalCartQuantitiesPromo() {
		HttpEntity<?> req = getHttpEntity("123");
		createCartForUser(req, 602L, 1);
		createCartForUser(req, 603L, 4);
		createCartForUser(req, 604L, 5);
		String requestBody = createCheckoutDTO().toString();
		req = new HttpEntity<>(requestBody, req.getHeaders());
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", req, Order.class);
		assertEquals(200, res.getStatusCodeValue());
		Order order = res.getBody();

		assertTrue(100 == order.getDiscount().intValue());
		assertTrue(1000 == order.getSubtotal().intValue());
		assertTrue(6.38 == order.getShipping().doubleValue());
		assertTrue("total is subTotal - discount + shipping", 906.38 == order.getTotal().doubleValue());
	}


	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_5.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderWithBuyXGetYPromoAndOtherCartPromos() {
		HttpEntity<?> req = getHttpEntity("123");
		createCartForUser(req, 601L, 3);
		createCartForUser(req, 603L, 1);

		var requestBody = createCheckoutDTO();
		requestBody.put("promo_code", "GREEEEEED");
		req = new HttpEntity<>(requestBody.toString(), req.getHeaders());
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", req, Order.class);
		assertEquals(200, res.getStatusCodeValue());
		Order order = res.getBody();

		assertTrue(340 == order.getDiscount().intValue());
		assertTrue(400 == order.getSubtotal().intValue());
		assertTrue(6.38 == order.getShipping().doubleValue());
		assertTrue("total is subTotal - discount + shipping", 66.38 == order.getTotal().doubleValue());
	}



	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_5.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderWithMultipleBuyXGetYPromoAndFirstInapplicable() {
		changePromoPriority(630006L, 3);
		promoRepo.deleteById(630002L);
		promoRepo.deleteById(630003L);

		HttpEntity<?> req = getHttpEntity("123");
		createCartForUser(req, 601L, 3);
		createCartForUser(req, 603L, 1);

		var requestBody = createCheckoutDTO();
		req = new HttpEntity<>(requestBody.toString(), req.getHeaders());
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", req, Order.class);
		assertEquals(200, res.getStatusCodeValue());
		Order order = res.getBody();

		assertEquals(100 , order.getDiscount().intValue());
		assertEquals(400 , order.getSubtotal().intValue());
		assertEquals(6.38 , order.getShipping().doubleValue(), 1e-15);
		assertEquals("total is subTotal - discount + shipping", 306.38 , order.getTotal().doubleValue(), 1e-15);
	}



	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Promotion_Test_Data_Insert_5.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void orderWithBuyXGetYPromoAndProductsInMultipleShops() {
		promoRepo.deleteById(630002L);
		promoRepo.deleteById(630003L);

		HttpEntity<?> req = getHttpEntity("123");
		createCartForUser(req, 601L, 3);
		createCartForUser(req, 602L, 1);

		var requestBody = createCheckoutDTO();
		req = new HttpEntity<>(requestBody.toString(), req.getHeaders());
		ResponseEntity<Order> res = template.postForEntity("/cart/checkout", req, Order.class);
		assertEquals(200, res.getStatusCodeValue());
		Order order = res.getBody();

		assertEquals(100 , order.getDiscount().intValue());
		assertEquals(400 , order.getSubtotal().intValue());
		assertEquals(12.76 , order.getShipping().doubleValue(), 1e-15);
		assertEquals("total is subTotal - discount + shipping", 312.76 , order.getTotal().doubleValue(), 1e-15);
	}

	private ItemsPromotionsDTO getApplicaPromotions(Set<String> productIds, Set<String> brandIds, Set<String> tagIds, Long promotionsPerItem) {
		List<String> queryParams = new LinkedList<>();
		if (!isNull(productIds))
			queryParams.add("product_ids=" + String.join(",", productIds));
		if (!isNull(brandIds))
			queryParams.add("brand_ids=" + String.join(",", brandIds));
		if (!isNull(tagIds))
			queryParams.add("tag_ids=" + String.join(",", tagIds));
		if (!isNull(promotionsPerItem))
			queryParams.add("promotions_per_item=" + promotionsPerItem);

		String queryString = queryParams.isEmpty() ? "" : "?" + String.join("&", queryParams);

		ResponseEntity<ItemsPromotionsDTO> res = template
				.getForEntity(
						"/navbox/applicable_promotions_list" + queryString,
						ItemsPromotionsDTO.class, queryParams);
		assertEquals(200, res.getStatusCodeValue());

		var responseBody = res.getBody();
		List<List<Long>> promoIdsFromLists = new LinkedList<>();
		promoIdsFromLists.addAll(responseBody.getProductPromotionIds().values());
		promoIdsFromLists.addAll(responseBody.getBrandPromotionIds().values());
		promoIdsFromLists.addAll(responseBody.getTagPromotionIds().values());

		Set<Long> promoIdsFromListsSet = promoIdsFromLists.stream().flatMap(Collection::stream).collect(Collectors.toSet());
		Map<Long, PromotionDTO> promosMap = responseBody.getPromotions();

		assertEquals(promoIdsFromListsSet, promosMap.keySet());

		assertTrue(promosMap.entrySet().stream().allMatch(entry -> entry.getKey().equals(entry.getValue().getId())));

		return responseBody;
	}

	
	@Test
	@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "/sql/Promotion_Test_Data_Insert_6.sql" })
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
	public void getApplicablePromotionsList() throws JsonProcessingException {
		ItemsPromotionsDTO applicablePromotions = getApplicaPromotions(setOf("1001", "1005"), setOf("2103"), setOf("22001"),
				10L);
		var productPromotions = applicablePromotions.getProductPromotionIds();
		assertEquals(2, productPromotions.size());
		assertEquals(List.of(99007L, 99006L), productPromotions.get(1001L));
		assertEquals(List.of(99007L, 99006L), productPromotions.get(1005L));
		var brandsPromotions = applicablePromotions.getBrandPromotionIds();
		assertEquals(1, brandsPromotions.size());
		assertEquals(Collections.<Long>emptyList(), brandsPromotions.get(2103L));
		var tagsPromotions = applicablePromotions.getTagPromotionIds();
		assertEquals(1, tagsPromotions.size());
		assertEquals(List.of(99007L, 99006L, 99008L), tagsPromotions.get(22001L));

		applicablePromotions = getApplicaPromotions(setOf("1001", "1005"), null, null,
				null);
		productPromotions = applicablePromotions.getProductPromotionIds();
		assertEquals(2, productPromotions.size());
		assertEquals(List.of(99007L), productPromotions.get(1001L));
		assertEquals(List.of(99007L), productPromotions.get(1005L));
		brandsPromotions = applicablePromotions.getBrandPromotionIds();
		assertTrue(brandsPromotions.isEmpty());
		tagsPromotions = applicablePromotions.getTagPromotionIds();
		assertTrue(tagsPromotions.isEmpty());
	}



	private void changePromoPriority(Long id, int priority) {
		promoRepo
			.findById(id)
			.map(e -> {e.setPriority(priority); return e;})
			.map(promoRepo::save);
	}


	private JSONObject createCheckoutDTO() {
		return json()
				.put("customer_address", 12300001)
				.put("shipping_service_id", "TEST");
	}
}
