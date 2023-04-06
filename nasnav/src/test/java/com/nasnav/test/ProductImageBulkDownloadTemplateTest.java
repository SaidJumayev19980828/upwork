package com.nasnav.test;

import static com.nasnav.enumerations.ImageFileTemplateType.PRODUCTS_WITH_NO_IMGS;
import static com.nasnav.service.impl.CsvDataImportServiceImpl.IMG_CSV_BASE_HEADERS;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:test.database.properties")
@NotThreadSafe 
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Product_Imgs_Custom_Repo_Test_Data.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class ProductImageBulkDownloadTemplateTest {
	private static final String PRODUCT_IMG_TEMPLATE_URL = "/product/image/bulk/template";
	
	private static final String ADMIN_TOKEN = "101112";
	private static final String EMPLOYEE_USER_TOKEN = "fssdfdsf";
	
	@Autowired
	private TestRestTemplate template;
	
	
	
	@Test
	public void getProductsWithNoImgsTemplateUserWithNoRightsTest() throws JsonProcessingException {				
		HttpEntity<?> request =  getHttpEntity("", EMPLOYEE_USER_TOKEN);		
		ResponseEntity<String> response =
				template.exchange(
						format(PRODUCT_IMG_TEMPLATE_URL+"?type=%s", PRODUCTS_WITH_NO_IMGS.name())
						, GET
						, request
						, String.class);		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void getProductsWithNoImgsTemplateTest() throws JsonProcessingException {
		HttpEntity<?> request =  getHttpEntity("", ADMIN_TOKEN);
		ResponseEntity<String> response =
				template.exchange(
						format(PRODUCT_IMG_TEMPLATE_URL+"?type=%s", PRODUCTS_WITH_NO_IMGS.name())
						, GET
						, request
						, String.class);
		assertEquals(OK, response.getStatusCode());		
	}

	@Test
	public void getEmptyImgsTemplateTest() throws JsonProcessingException {
		HttpEntity<?> request =  getHttpEntity("", ADMIN_TOKEN);
		ResponseEntity<String> response =
				template.exchange(
						PRODUCT_IMG_TEMPLATE_URL
						, GET
						, request
						, String.class);
		assertEquals(OK, response.getStatusCode());
		List<String> headers = 
				asList(response
						.getBody()
						.replace("\n", "")
						.split(","));
		assertEquals(IMG_CSV_BASE_HEADERS, headers);
	}
}
