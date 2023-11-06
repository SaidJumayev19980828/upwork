package com.nasnav.test;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import com.nasnav.dao.RocketChatEmployeeAgentRepository;
import com.nasnav.dao.RocketChatOrganizationDepartmentRepository;
import com.nasnav.dto.rocketchat.RocketChatAgentTokenDTO;
import com.nasnav.dto.rocketchat.RocketChatUserDTO;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.persistence.RocketChatEmployeeAgentEntity;
import com.nasnav.persistence.RocketChatOrganizationDepartmentEntity;
import com.nasnav.service.rocketchat.impl.RocketChatClient;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@NotThreadSafe
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "/sql/Chat_Test_Data.sql" })
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
class ChatApiTest extends AbstractTestWithTempBaseDir {
	@Autowired
	private TestRestTemplate template;

	@Autowired
	private RocketChatOrganizationDepartmentRepository rocketChatOrganizationDepartmentRepository;

	@Autowired
	private RocketChatEmployeeAgentRepository rocketChatEmployeeAgentRepository;

	@Autowired
	private RocketChatClient client;

	private static ClientAndServer mockServer;

	@BeforeAll
	public static void startServer() {
		mockServer = ClientAndServer.startClientAndServer(3080);
	}

	@AfterAll
	public static void stopServer() {
		mockServer.stop();
	}

	@BeforeEach
	void setupServerRules() {
		creatMockServerRole("GET", "/livechat/config", "{\"config\":{}}", 200);
		creatMockServerRole("DELETE", null, "{}", 200);
		creatMockServerRole("POST", "/livechat/visitor", "{\"visitor\":{}}", 200);
		creatMockServerRole("POST", "/livechat/department", "{\"department\":{\"_id\":\"sdfgbn\"}}", 200);
		creatMockServerRole("POST", "/chat/agent/authenticate", "{\"department\":{\"_id\":\"sdfgbn\"}}", 200);
		creatMockServerRole("POST", "/users.create", "{\"user\":{\"_id\":\"fdgfdg\",\"username\":\"wcrtc\"}}", 200);
		creatMockServerRole("POST", "/livechat/users/agent", "{\"user\":{\"_id\":\"fdgfdg\",\"username\":\"wcrtc\"}}", 200);
		creatMockServerRole("POST", "/livechat/department/sdfgbn/agents", "", 200);
		creatMockServerRole("POST", "/users.delete", "", 200);
		createUserTokenRoles();
	}

	@AfterEach
	void cleanup() {
		StepVerifier.create(Flux.fromIterable(rocketChatOrganizationDepartmentRepository.findAll())
				.map(RocketChatOrganizationDepartmentEntity::getDepartmentId)
				.flatMap(client::deleteDepartment))
				.verifyComplete();
		StepVerifier.create(Flux.fromIterable(rocketChatEmployeeAgentRepository.findAll())
				.map(RocketChatEmployeeAgentEntity::getUsername)
				.map(username -> RocketChatUserDTO.builder()
						.username(username)
						.build())
				.flatMap(client::deleteUser))
				.verifyComplete();
	}

	@Test
	void getVisitorData() {
		HttpEntity<?> request = getHttpEntity("abc");
		ResponseEntity<RocketChatVisitorDTO> response = template.exchange("/chat/visitor", POST, request,
				RocketChatVisitorDTO.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		createDepartmentRoles();
		response = template.exchange("/chat/visitor", POST, request,
				RocketChatVisitorDTO.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void createAgentToken() {
		HttpEntity<?> request = getHttpEntity("qwe");
		final ResponseEntity<RocketChatAgentTokenDTO> response1 = template.exchange("/chat/agent/authenticate", POST, request,
				RocketChatAgentTokenDTO.class);
		assertEquals(HttpStatus.OK, response1.getStatusCode());
		assertNotNull(response1.getBody().getAuthToken());

		createDepartmentRoles();
		createUserTokenRoles();
		final ResponseEntity<RocketChatAgentTokenDTO> response2 = template.exchange("/chat/agent/authenticate", POST, request,
				RocketChatAgentTokenDTO.class);
		assertEquals(HttpStatus.OK, response2.getStatusCode());
		assertNotNull(response2.getBody().getAuthToken());

		assertEquals(response1.getBody().getUserId(), response2.getBody().getUserId());
		assertNotEquals(response1.getBody().getAuthToken(), response2.getBody().getAuthToken());
	}

	private void createDepartmentRoles() {
		rocketChatOrganizationDepartmentRepository.findAll().forEach(dep -> {
			creatMockServerRole("GET", "/livechat/department/" + dep.getDepartmentId(), String.format("{\"department\":{\"_id\": \"%s\"}}", dep.getDepartmentId()), 200);
		});
	}

	private void createUserTokenRoles() {
		creatMockServerRole("POST", "/users.createToken", String.format("{\"data\":{\"authToken\":\"%s\"}}", UUID.randomUUID().toString()), 200);
	}

	private static void creatMockServerRole(String method, String path, String response, int statusCode) {
		HttpRequest request = request();

		if (method != null) {
			request = request.withMethod(method);
		}

		if (path != null) {
			request = request.withPath(path);
		}

		mockServer.clear(request);
		mockServer.when(request).respond(response(response).withStatusCode(statusCode).withHeader("Content-Type", "application/json"));
	}
}
