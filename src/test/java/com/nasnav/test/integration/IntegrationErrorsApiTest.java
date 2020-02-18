package com.nasnav.test.integration;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static java.time.LocalDateTime.now;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.nasnav.NavBox;
import com.nasnav.dto.IntegrationErrorDTO;
import com.nasnav.dto.ResponsePage;

import net.jcip.annotations.NotThreadSafe;

@SuppressWarnings("deprecation")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@DirtiesContext
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Error_Api_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class IntegrationErrorsApiTest {
	private static final int PAGE_NUM = 2;
	private static final int PAGE_SIZE = 2;
	private static final String NASNAV_ADMIN_TOKEN = "abcdefg";
	private static final String ORG_EMP_TOKEN = "sfeesdfsdf";
	private static final String ORG_ADMIN_TOKEN = "hijkllm";
	private static final Long ORG_ID = 99001L;
	
	@Autowired
	private TestRestTemplate template;
	
	
	
	
	
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
	public void testGetIntegrationErrorsDifferentOrgAdmin() throws JsonParseException, JsonMappingException, IOException {
		
		String url = 
				UriComponentsBuilder
					.fromPath("/integration/errors")
					.queryParam("org_id", 99002)
					.queryParam("page_size", PAGE_SIZE)
					.queryParam("page_num", PAGE_NUM)
					.build()
					.toString();
		HttpEntity<?> request =  getHttpEntity("", ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, GET
						, request
						, String.class);
		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testGetIntegrationErrorsNasnavAdmin() throws JsonParseException, JsonMappingException, IOException {
		
		String url = 
				UriComponentsBuilder
					.fromPath("/integration/errors")
					.queryParam("org_id", ORG_ID)
					.queryParam("page_size", PAGE_SIZE)
					.queryParam("page_num", PAGE_NUM)
					.build()
					.toString();
		HttpEntity<?> request =  getHttpEntity("", NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, GET
						, request
						, String.class);
		
		assertEquals(OK, response.getStatusCode());
		//----------------------------------------------------------
		assertValidResponse(response);
	}
	
	
	
	
	
	@Test
	public void testGetIntegrationErrors() throws JsonParseException, JsonMappingException, IOException {
		
		String url = 
				UriComponentsBuilder
					.fromPath("/integration/errors")
					.queryParam("org_id", ORG_ID)
					.queryParam("page_size", PAGE_SIZE)
					.queryParam("page_num", PAGE_NUM)
					.build()
					.toString();
					
		HttpEntity<?> request =  getHttpEntity("", ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, GET
						, request
						, String.class);
		
		assertEquals(OK, response.getStatusCode());
		//----------------------------------------------------------
		assertValidResponse(response);
	}






	private void assertValidResponse(ResponseEntity<String> response)
			throws IOException, JsonParseException, JsonMappingException {
		ResponsePage<IntegrationErrorDTO> page = readGetIntegrationErrorsResponse(response); 
		assertEquals(PAGE_SIZE, page.getPageSize().intValue());
		assertEquals(PAGE_NUM, page.getPageNumber().intValue());
		assertEquals(6, page.getTotalElements().intValue());
		assertEquals(3, page.getTotalPages().intValue());
		
		List<IntegrationErrorDTO> errors = page.getContent();		
		assertTrue(allErrorsAreSince4Days(errors));
	}






	private boolean allErrorsAreSince4Days(List<IntegrationErrorDTO> errors) {
		return errors
				.stream()
				.allMatch(err -> err.getCreatedAt().isAfter(now().minusDays(4)));
	}
	
	
	
	
	private ResponsePage<IntegrationErrorDTO> readGetIntegrationErrorsResponse(ResponseEntity<String> response)
			throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JSR310Module());
		TypeReference<ResponsePage<IntegrationErrorDTO>> typeRef = 
				new TypeReference<ResponsePage<IntegrationErrorDTO>>() {};
		return mapper.readValue(response.getBody(), typeRef);
	}
}
