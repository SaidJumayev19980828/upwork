package com.nasnav.yeshtery.test;

import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.response.PostResponseDTO;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/posts_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class PostTest extends AbstractTestWithTempBaseDir {
	@Autowired
	private TestRestTemplate template;

	@Test
	public void getForYouUserWithException()
	{
		int start = 0, count = 10;
		ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
				"/v1/post/filterForUser?userId=" + 9999 + "&start=" + start + "&count=" + count + "&type=reviews", HttpMethod.GET,
				getHttpEntity("abcdefg"), new ParameterizedTypeReference<>()
				{
				});
		assertEquals(403, response.getStatusCode().value());
	}

	@Test
	public void getForYouUserWithNotException()
	{
		int start = 0, count = 10;
		ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
				"/v1/post/filterForUser?userId=" + 9999 + "&start=" + start + "&count=" + count + "&type=reviews", HttpMethod.GET, getHttpEntity("123"),
				new ParameterizedTypeReference<>()
				{
				});
		assertEquals(404, response.getStatusCode().value());
	}

	@Test
	public void getForYouUser()
	{
		int start = 0, count = 10;
		ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
				"/v1/post/filterForUser?userId=" + 88 + "&start=" + start + "&count=" + count + "&type=following", HttpMethod.GET, getHttpEntity("123"),
				new ParameterizedTypeReference<>()
				{
				});
		assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void getForYouUserExplore()
	{
		int start = 0, count = 10;
		ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
				"/v1/post/filterForUser?userId=" + 88 + "&start=" + start + "&count=" + count + "&type=explore", HttpMethod.GET, getHttpEntity("123"),
				new ParameterizedTypeReference<>()
				{
				});
		assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void getForYouUserReview()
	{
		int start = 0, count = 10;
		ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
				"/v1/post/filterForUser?userId=" + 88 + "&start=" + start + "&count=" + count + "&type=reviews", HttpMethod.GET, getHttpEntity("123"),
				new ParameterizedTypeReference<>()
				{
				});
		assertEquals(200, response.getStatusCode().value());
	}

	public void getForYouUserWithoutType()
	{
		int start = 0, count = 10;
		ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
				"/v1/post/filterForUser?userId=" + 88 + "&start=" + start + "&count=" + count, HttpMethod.GET, getHttpEntity("123"),
				new ParameterizedTypeReference<>()
				{
				});
		assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void getForYouUserWrongType()
	{
		int start = 0, count = 10;
		ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
				"/v1/post/filterForUser?userId=" + 88 + "&start=" + start + "&count=" + count + "&type=worng", HttpMethod.GET, getHttpEntity("123"),
				new ParameterizedTypeReference<>()
				{
				});
		assertEquals(200, response.getStatusCode().value());
	}
}
