package com.nasnav.yeshtery.test.controllers.yeshtery;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.concurrent.NotThreadSafe;

import com.nasnav.dao.ProductRepository;
import com.nasnav.persistence.ProductEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import com.nasnav.dto.ProductsFiltersResponse;
import com.nasnav.dto.ProductsResponse;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.dto.response.navbox.VariantsResponse;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class ProductFiltersTest extends AbstractTestWithTempBaseDir {
  @Autowired
	private TestRestTemplate template;
	@Autowired
	private MockMvc mockMvc;

	@Mock
	ProductRepository productRepository;

	@Test
  void testGetProducts() {
		ResponseEntity<ProductsResponse> response =
				template.getForEntity("/v1/yeshtery/products", ProductsResponse.class);
		final Long total = response.getBody().getTotal();
		assertEquals(3L, total);
  }

	@Test
	public void get360Product() throws Exception {

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get("/v1/yeshtery/products/360")
				.accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		assertEquals(200,result.getResponse().getStatus());
		assertNotNull(result.getResponse().getContentAsString());
	}

	@Test
	public void get360ProductsServiceTest(){
		List<ProductEntity> products=productRepository.get360Products();
		assertNotNull(products);
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

	@ParameterizedTest
	@CsvSource({ "-1,-1", "0,1", "0,1000" })
	void listVariants(Integer start, Integer count) {
		ResponseEntity<VariantsResponse> response = template.getForEntity(
				"/v1/yeshtery/variants?start={start}&count={count}", VariantsResponse.class, start,
				count);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(7, response.getBody().getTotal());
		if (start > 0 && count > 0)
			assertEquals(Math.min(count, 7 - start), response.getBody().getVariants().size());
	}
}
