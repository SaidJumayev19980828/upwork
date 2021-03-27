package com.nasnav.test;

import com.nasnav.NavBox;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:test.database.properties")
@NotThreadSafe 
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD , scripts = {"/sql/Products_Export_Test_Data.sql"})
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD , scripts = {"/sql/database_cleanup.sql"})
public class DataExportTest {
	
	
	@Autowired
	private TestRestTemplate template;

	@Test
	public void testProductExportCsv(){
		ResponseEntity<String> response = 
				template.exchange("/export/products?shop_id=502&type=CSV", GET, getHttpEntity("192021"), String.class);
		
		System.out.println(">>>>>>>\n" + response.getBody());
		
		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductExportCsvShopFromAnotherOrganization(){
		ResponseEntity<String> response =
				template.exchange("/export/products?shop_id=501&type=CSV", GET, getHttpEntity("192021"), String.class);

		System.out.println(">>>>>>>\n" + response.getBody());

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}

	@Test
	public void testProductExportCsvForWholeOrganization(){
		ResponseEntity<String> response =
				template.exchange("/export/products?type=CSV", GET, getHttpEntity("192021"), String.class);

		System.out.println(">>>>>>>\n" + response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductExportCsvNoAuthz(){
		ResponseEntity<String> response = 
				template.exchange("/export/products?shop_id=502&type=CSV", GET, getHttpEntity("101112"), String.class);
		
		System.out.println(">>>>>>>\n" + response.getBody());
		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}


	@Test
	public void testProductExportXlsx(){
		ResponseEntity<String> response =
				template.exchange("/export/products?shop_id=502&type=XLSX", GET, getHttpEntity("192021"), String.class);

		System.out.println(">>>>>>>\n" + response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductExportXlsxShopFromAnotherOrganization(){
		ResponseEntity<String> response =
				template.exchange("/export/products?shop_id=501&type=XLSX", GET, getHttpEntity("192021"), String.class);

		System.out.println(">>>>>>>\n" + response.getBody());

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}

	@Test
	public void testProductExportXlsxForWholeOrganization(){
		ResponseEntity<String> response =
				template.exchange("/export/products?type=XLSX", GET, getHttpEntity("192021"), String.class);

		System.out.println(">>>>>>>\n" + response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductExportXlsxNoAuthz(){
		ResponseEntity<String> response =
				template.exchange("/export/products?shop_id=502&type=XLSX", GET, getHttpEntity("101112"), String.class);

		System.out.println(">>>>>>>\n" + response.getBody());

		assertEquals(FORBIDDEN, response.getStatusCode());
	}
}
