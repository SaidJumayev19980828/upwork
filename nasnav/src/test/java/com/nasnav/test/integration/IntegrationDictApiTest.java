package com.nasnav.test.integration;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nasnav.dto.IntegrationDictionaryDTO;
import com.nasnav.dto.ResponsePage;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringRunner.class)
@NotThreadSafe
// @DirtiesContext
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Dict_Api_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class IntegrationDictApiTest extends AbstractTestWithTempBaseDir {
	private static final int PAGE_NUM = 2;
	private static final int PAGE_SIZE = 2;
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
	public void testGetIntegrationDictsDifferentOrgAdmin() throws JsonParseException, JsonMappingException, IOException {		
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
	public void testGetIntegrationDict() throws JsonParseException, JsonMappingException, IOException {
		
		String url = 
				UriComponentsBuilder
					.fromPath("/integration/dictionary")
					.queryParam("org_id", ORG_ID)
					.queryParam("page_size", PAGE_SIZE)
					.queryParam("page_num", PAGE_NUM)
					.queryParam("dict_type", PRODUCT_VARIANT.getValue())
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
	
	
	
	
	
	
	@Test
	public void testGetIntegrationDictUsingNasnavAdmin() throws JsonParseException, JsonMappingException, IOException {
		
		String url = 
				UriComponentsBuilder
					.fromPath("/integration/dictionary")
					.queryParam("org_id", ORG_ID)
					.queryParam("page_size", PAGE_SIZE)
					.queryParam("page_num", PAGE_NUM)
					.queryParam("dict_type", PRODUCT_VARIANT.getValue())
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
	
	
	
	
	
	
	
	private void assertValidResponse(ResponseEntity<String> response)
			throws IOException, JsonParseException, JsonMappingException {
		ResponsePage<IntegrationDictionaryDTO> page = readGetIntegrationErrorsResponse(response); 
		assertEquals(PAGE_SIZE, page.getPageSize().intValue());
		assertEquals(PAGE_NUM, page.getPageNumber().intValue());
		assertEquals(6, page.getTotalElements().intValue());
		assertEquals(3, page.getTotalPages().intValue());
		
		List<IntegrationDictionaryDTO> errors = page.getContent();		
	}
	
	
	
	
	private ResponsePage<IntegrationDictionaryDTO> readGetIntegrationErrorsResponse(ResponseEntity<String> response)
			throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		TypeReference<ResponsePage<IntegrationDictionaryDTO>> typeRef = 
				new TypeReference<ResponsePage<IntegrationDictionaryDTO>>() {};
		return mapper.readValue(response.getBody(), typeRef);
	}
}
