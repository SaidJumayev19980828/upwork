package com.nasnav.yeshtery.test;

import com.nasnav.dto.ProductStocksDTO;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Products_API_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ProductApiTest extends AbstractTestWithTempBaseDir {

	@Autowired
	private TestRestTemplate template;





	@Test
	public void getYeshteryProducts() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("User-Token", "131415");
		HttpEntity<ProductStocksDTO> request = new HttpEntity<>(headers);
		ResponseEntity<List> response = template.exchange("/v1/yeshtery/products_by_ids?ids=1001,1003,1004", HttpMethod.GET, request, List.class);
		assertEquals(200, response.getStatusCodeValue());
		List resBody=response.getBody();
		assertNotNull(resBody);
		assertEquals(resBody.size(), 3);
	}

	@Test
	public void getYeshteryProductsMissedIds() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("User-Token", "131415");
		HttpEntity<ProductStocksDTO> request = new HttpEntity<>(headers);
		ResponseEntity<List> response = template.exchange("/v1/yeshtery/products_by_ids?ids=1001,1003,11004", HttpMethod.GET, request, List.class);
		assertEquals(200, response.getStatusCodeValue());
		List resBody=response.getBody();
		assertNotNull(resBody);
		assertEquals(resBody.size(), 2);
	}



}
