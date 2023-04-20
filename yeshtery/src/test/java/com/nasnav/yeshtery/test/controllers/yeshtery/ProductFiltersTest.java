package com.nasnav.yeshtery.test.controllers.yeshtery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import com.nasnav.dto.ProductsFiltersResponse;
import com.nasnav.dto.ProductsResponse;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.yeshtery.Yeshtery;

@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class ProductFiltersTest {
  @Autowired
	private TestRestTemplate template;

  @Test
  void testGetProducts() {
		ResponseEntity<ProductsResponse> response =
				template.getForEntity("/v1/yeshtery/products", ProductsResponse.class);
		final Long total = response.getBody().getTotal();
		assertEquals(3L, total);
  }

	@Test
  void testGetFiltersWithTagsOrgId() {

		ResponseEntity<ProductsFiltersResponse> response =
				template.getForEntity("/v1/yeshtery/filters?tags_org_id=99003", ProductsFiltersResponse.class);
		assertEquals(200, response.getStatusCodeValue());

		Set<Long> expectedtagIds = Set.of(22003L, 22004L);
		Set<Long> foundTagIds = response.getBody().getTags().stream().map(TagsRepresentationObject::getId).collect(Collectors.toSet());

		assertEquals(expectedtagIds, foundTagIds);
  }
}
