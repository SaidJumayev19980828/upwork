package com.nasnav.yeshtery.test.controllers.yeshtery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.ErrorResponseDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.yeshtery.Yeshtery;

@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "/sql/Promotion_Test_Data_Insert.sql" })
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
class ActivePromotionsTest {
	@Autowired
	private TestRestTemplate template;

	@Test
	void getActivePromotions() {
		ResponseEntity<List<PromotionDTO>> res = template
				.exchange(
						"/v1/yeshtery/active_promotions?org_ids=99001&type_ids=1",
						HttpMethod.GET,
						null,
						new ParameterizedTypeReference<List<PromotionDTO>>() {
						});
		assertEquals(200, res.getStatusCodeValue());

		List<Long> promotionIds = res.getBody().stream().map(PromotionDTO::getId).toList();
		assertEquals(List.of(), promotionIds);
	}

	@Test
	void getActivePromotionsWithoutFilter() {
		ResponseEntity<List<PromotionDTO>> res = template
				.exchange(
						"/v1/yeshtery/active_promotions",
						HttpMethod.GET,
						null,
						new ParameterizedTypeReference<List<PromotionDTO>>() {
						});
		assertEquals(200, res.getStatusCodeValue());

		List<Long> promotionIds = res.getBody().stream().map(PromotionDTO::getId).toList();
		assertEquals(List.of(630002L, 630003L), promotionIds);
	}


	@Test
	void getActivePromotionsWithNonYeshteryOrganization() {
		ResponseEntity<ErrorResponseDTO> res = template
				.exchange(
						"/v1/yeshtery/active_promotions?org_ids=99002",
						HttpMethod.GET,
						null,
						new ParameterizedTypeReference<ErrorResponseDTO>() {
						});
		assertEquals(406, res.getStatusCodeValue());
		assertEquals(ErrorCodes.PROMO$PARAM$0019.toString(), res.getBody().getError());
	}
}
