package com.nasnav.yeshtery.test;

import com.nasnav.dto.*;
import com.nasnav.dto.response.*;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "/sql/followers_Test_Data.sql" })
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
public class FollowerTest extends AbstractTestWithTempBaseDir {
	@Autowired
	private TestRestTemplate template;

	@Test
	public void getForFollowersData() {
		int start = 0, count = 10;
		ResponseEntity<RestResponsePage<FollowerDTO>> response = template.exchange(
				"/v1/follow/follower?userId=" + 88 + "&start=" + start + "&count=" + count, HttpMethod.GET, getHttpEntity("778"),
				new ParameterizedTypeReference<>() {
				});
		assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void getForFollowingsData() {
		int start = 0, count = 10;
		ResponseEntity<PaginatedResponse<UserRepresentationObject>> response = template.exchange(
				"/v1/follow/following?followerId=" + 88 + "&start=" + start + "&count=" + count, HttpMethod.GET, getHttpEntity("778"),
				new ParameterizedTypeReference<>() {
				});
		assertEquals(200, response.getStatusCode().value());
	}

}
