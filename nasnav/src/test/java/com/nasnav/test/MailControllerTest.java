package com.nasnav.test;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.lang.reflect.Field;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import javax.annotation.concurrent.NotThreadSafe;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import com.nasnav.service.sendpulse.Sendpulse;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

@NotThreadSafe
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "/sql/Mail_Test_Data.sql" })
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
class MailControllerTest extends AbstractTestWithTempBaseDir {
	@Autowired
	TestRestTemplate template;

	private static ClientAndServer mockServer;

	@BeforeAll
	public static void startServer() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = Sendpulse.class.getDeclaredField("apiUrl");
        field.setAccessible(true);
        field.set(null, "http://127.0.0.1:1080");

		mockServer = ClientAndServer.startClientAndServer(1080);
		mockServer.when(
				request().withMethod("POST")).respond(response("{\"access_token\":\"abc\"}").withStatusCode(200));
	}

	@AfterAll
	public static void stopServer() {
		mockServer.stop();
	}

	@Test
	void sendCartEmail() {
		HttpEntity<?> request = getHttpEntity("{\"promo\":\"GREEEEEED\",\"user_ids\":[88]}", "101112");
		ResponseEntity<Void> response = template.exchange("/mail/cart/abandoned", POST, request, Void.class);
		assertEquals(OK, response.getStatusCode());
	}

	@Test
	void sendWishlistEmail() {
		HttpEntity<?> request = getHttpEntity("101112");
		ResponseEntity<Void> response = template.exchange("/mail/wishlist/stock", POST, request, Void.class);
		assertEquals(OK, response.getStatusCode());
	}
}
