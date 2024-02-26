package com.nasnav.test;

import com.nasnav.response.GenerateOrganizationPannerResponse;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.util.UriComponentsBuilder;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringJUnit4ClassRunner.class)
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "/sql/generateImage.sql" })
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
public class GenerateImageApiTest extends AbstractTestWithTempBaseDir
{

	@Autowired
	private TestRestTemplate template;

	@Test
	public void generateImage_Test()
	{
		//
		String encodedMultiWordString = UriComponentsBuilder.newInstance()
				.queryParam("chattxt", "I want an image for chocolate birthday cake with 28 written on top of it").build().encode().toUriString();
		String urlWithParams = "/organization/create_image?orgId=99001&description=" + encodedMultiWordString + "&oldPath=sdkd";
		HttpEntity<?> json = getHttpEntity("123");
		ResponseEntity<GenerateOrganizationPannerResponse> response = template.exchange(urlWithParams, HttpMethod.PUT, json,
				GenerateOrganizationPannerResponse.class);
		assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void generateImageAuth_failed_Test()
	{
		//
		String encodedMultiWordString = UriComponentsBuilder.newInstance()
				.queryParam("chattxt", "I want an image for chocolate birthday cake with 28 written on top of it").build().encode().toUriString();
		String urlWithParams = "/organization/create_image?orgId=99001&description=" + encodedMultiWordString + "&oldPath=sdkd";
		HttpEntity<?> json = getHttpEntity("1111");
		ResponseEntity<GenerateOrganizationPannerResponse> response = template.exchange(urlWithParams, HttpMethod.PUT, json,
				GenerateOrganizationPannerResponse.class);
		assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void generateImageOrgId_failed_Test()
	{
		String encodedMultiWordString = UriComponentsBuilder.newInstance()
				.queryParam("chattxt", "I want an image for chocolate birthday cake with 28 written on top of it").build().encode().toUriString();
		String urlWithParams = "/organization/create_image?orgId=1&description=" + encodedMultiWordString + "&oldPath=sdkd";
		HttpEntity<?> json = getHttpEntity("123");
		ResponseEntity<GenerateOrganizationPannerResponse> response = template.exchange(urlWithParams, HttpMethod.PUT, json,
				GenerateOrganizationPannerResponse.class);
		assertEquals(404, response.getStatusCode().value());
	}

	@Test
	public void generateImageRequest_failed_Test()
	{
		String urlWithParams = "/organization/create_image?oldPath=sdkd";
		HttpEntity<?> json = getHttpEntity("123");
		ResponseEntity<GenerateOrganizationPannerResponse> response = template.exchange(urlWithParams, HttpMethod.PUT, json,
				GenerateOrganizationPannerResponse.class);
		assertEquals(400, response.getStatusCode().value());
	}
}
