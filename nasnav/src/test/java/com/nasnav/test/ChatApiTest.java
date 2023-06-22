package com.nasnav.test;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;

@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Chat_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ChatApiTest extends AbstractTestWithTempBaseDir {
	@Autowired
	private TestRestTemplate template;

	@Test
	void getVisitorData() {
		HttpEntity<?> request = getHttpEntity("abc");
		ResponseEntity<RocketChatVisitorDTO> response = template.exchange("/chat/visitor_data", GET, request,
				RocketChatVisitorDTO.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
}
