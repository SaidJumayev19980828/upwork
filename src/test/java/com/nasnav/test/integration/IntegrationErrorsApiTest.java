package com.nasnav.test.integration;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import com.nasnav.test.commons.TestCommons;

import com.nasnav.NavBox;

import net.jcip.annotations.NotThreadSafe;
import net.minidev.json.JSONObject;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@DirtiesContext
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Api_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class IntegrationErrorsApiTest {
	private static final String NASNAV_ADMIN_TOKEN = "abcdefg";
	private static final String ORG_EMP_TOKEN = "sfeesdfsdf";
	private static final String ORG_ADMIN_TOKEN = "hijkllm";
	private static final Long ORG_ID = 99001L;
	
	@Autowired
	private TestRestTemplate template;
	
	@Test
	public void testGetIntegrationDictNoAuthZ() {
		String url = "/integration/dictionary";
		HttpEntity<?> request =  getHttpEntity("{}", "NON_EXISTING_TOKEN");
		
		ResponseEntity<String> response = 
				template.exchange(url
						, GET
						, request
						, String.class);
		
		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	public void testGetIntegrationDictNoAuthN() {
		String url = "/integration/dictionary";
		HttpEntity<?> request =  getHttpEntity("{}", ORG_EMP_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, GET
						, request
						, String.class);
		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testGetIntegrationErrorsNoAuthZ() {
		String url = "/integration/errors";
		HttpEntity<?> request =  getHttpEntity("{}", "NON_EXISTING_TOKEN");
		
		ResponseEntity<String> response = 
				template.exchange(url
						, GET
						, request
						, String.class);
		
		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testGetIntegrationErrorsNoAuthN() {
		String url = "/integration/errors";
		HttpEntity<?> request =  getHttpEntity("{}", ORG_EMP_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, GET
						, request
						, String.class);
		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Error_Api_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void testGetIntegrationErrors() {
		
		String url = "/integration/errors";
		String requestJson = 
					json()
					 .put("org_id", ORG_ID)
					 .put("page_size", 2)
					 .put("page_num", 2)
					.toString();
					
		HttpEntity<?> request =  getHttpEntity(requestJson, ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, GET
						, request
						, String.class);
		
		assertEquals(OK, response.getStatusCode());
		//----------------------------------------------------------
		
	}
}
