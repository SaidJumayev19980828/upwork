package com.nasnav.yeshtery.test.controllers;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

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

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dao.RocketChatOrganizationDepartmentRepository;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.persistence.RocketChatOrganizationDepartmentEntity;
import com.nasnav.service.rocketchat.impl.RocketChatClient;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@NotThreadSafe
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "/sql/Chat_Test_Data.sql" })
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
class ChatApiTest extends AbstractTestWithTempBaseDir {
	private static final String VISITOR_PATH = YeshteryConstants.API_PATH + "/chat/visitor?org_id=99001";

	@Autowired
	private TestRestTemplate template;

	@Autowired
	private RocketChatOrganizationDepartmentRepository rocketChatOrganizationDepartmentRepository;

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
	}

	@AfterEach
	void cleanup() {
		StepVerifier.create(Flux.fromIterable(rocketChatOrganizationDepartmentRepository.findAll())
				.map(RocketChatOrganizationDepartmentEntity::getDepartmentId)
				.flatMap(client::deleteDepartment))
				.verifyComplete();
	}

	@Test
	void getVisitorData() {
		HttpEntity<?> request = getHttpEntity("abc");
		ResponseEntity<RocketChatVisitorDTO> response = template.exchange(VISITOR_PATH, POST, request,
				RocketChatVisitorDTO.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		createDepartmentRoles();
		response = template.exchange(VISITOR_PATH, POST, request,
				RocketChatVisitorDTO.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	private void createDepartmentRoles() {
		rocketChatOrganizationDepartmentRepository.findAll().forEach(dep -> {
			creatMockServerRole("GET", "/livechat/department/" + dep.getDepartmentId(), String.format("{\"department\":{\"_id\": \"%s\"}}", dep.getDepartmentId()), 200);
		});
	}

	private static void creatMockServerRole(String method, String path, String response, int statusCode) {
		HttpRequest request = request();

		if (method != null) {
			request = request.withMethod(method);
		}

		if (path != null) {
			request = request.withPath(path);
		}

		mockServer.when(request).respond(response(response).withStatusCode(statusCode).withHeader("Content-Type", "application/json"));
	}
}
