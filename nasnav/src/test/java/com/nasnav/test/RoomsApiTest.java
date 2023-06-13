package com.nasnav.test;

import com.nasnav.NavBox;
import com.nasnav.dao.RoomTemplateRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.response.RoomResponse;
import com.nasnav.mappers.RoomMapper;
import com.nasnav.persistence.RoomTemplateEntity;

import net.jcip.annotations.NotThreadSafe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static com.nasnav.test.commons.TestCommons.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/Room_Api_Test_Data.sql")
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/database_cleanup.sql")
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
class RoomsApiTest {

	@Autowired
	private TestRestTemplate template;
	@Autowired
	private RoomMapper mapper;
	@Autowired
	private RoomTemplateRepository roomTemplateRepository;
	@Autowired
	private UserRepository userRepository;

	@Test
	void getRoomsByUserToken() {
		userRepository.findAll().forEach(user -> {
			Set<RoomResponse> rooms = roomTemplateRepository.findAllByShopOrganizationEntityId(user.getOrganizationId())
					.stream()
					.map(mapper::toRoomResponse).collect(Collectors.toSet());
			HttpEntity<Object> request = getHttpEntity(user.getAuthenticationToken());
			ResponseEntity<Set<RoomResponse>> res = template
					.exchange("/room/list_for_user", HttpMethod.GET, request,
							new ParameterizedTypeReference<Set<RoomResponse>>() {
							});
			assertEquals(HttpStatus.OK, res.getStatusCode());
			Set<RoomResponse> body = res.getBody();
			assertEquals(rooms, body);
		});
	}

	@Test
	void getRoomsByOrgId() {
		List.of(99001L, 99002L).forEach(orgId -> {
			Set<RoomResponse> rooms = roomTemplateRepository.findAllByShopOrganizationEntityId(orgId)
					.stream()
					.map(mapper::toRoomResponse).collect(Collectors.toSet());
			HttpEntity<Object> request = getHttpEntity(null);
			ResponseEntity<Set<RoomResponse>> res = template
					.exchange("/room/list?org_id={orgId}", HttpMethod.GET, request,
							new ParameterizedTypeReference<Set<RoomResponse>>() {
							}, orgId);
			assertEquals(HttpStatus.OK, res.getStatusCode());
			Set<RoomResponse> body = res.getBody();
			assertEquals(rooms, body);
		});
	}

	@Test
	void getSingleRoom() {
		RoomResponse room = roomTemplateRepository.findById(501L)
				.map(mapper::toRoomResponse).get();
		ResponseEntity<RoomResponse> res = template
				.getForEntity("/room?shop_id=51",
						RoomResponse.class);
		assertEquals(HttpStatus.OK, res.getStatusCode());
		RoomResponse body = res.getBody();
		assertEquals(room, body);
	}

	@Test
	void setRoomSession() {
		HttpEntity<Object> request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user81");
		LocalDateTime before = LocalDateTime.now();
		ResponseEntity<RoomResponse> res = template
				.postForEntity("/room/session?shop_id=51", request,
						RoomResponse.class);
		LocalDateTime after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		RoomResponse body = res.getBody();
		assertRoomResponse(body, before, after, "test_new_session", "user81@nasnav.com");

		request = getHttpEntity("{\"session_external_id\":\"test_new_session_2\"}", "user81");
		before = LocalDateTime.now();
		res = template
				.postForEntity("/room/session?shop_id=52", request,
						RoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		body = res.getBody();
		assertRoomResponse(body, before, after, "test_new_session_2", "user81@nasnav.com");

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user82");
		ResponseEntity<String> res2 = template
				.postForEntity("/room/session?shop_id=51", request,
						String.class);
		assertEquals(HttpStatus.NOT_FOUND, res2.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user82");
		res2 = template
				.postForEntity("/room/session?shop_id=53", request,
						String.class);
		assertEquals(HttpStatus.NOT_FOUND, res2.getStatusCode());
	}

	@Test
	void postTemplate() {
		String requestBody = "{\"scene_id\": \"someId\", \"data\": \"some data\"}";
		assertTemplateRequest("user81", requestBody, 51L, HttpStatus.FORBIDDEN);
		assertTemplateRequest("101112", requestBody, 51L, HttpStatus.FORBIDDEN);
		assertTemplateRequest("131415", requestBody, 51L, HttpStatus.OK);
		assertTemplateRequest("161718", requestBody, 51L, HttpStatus.NOT_FOUND);
		assertTemplateRequest("192021", requestBody, 52L, HttpStatus.OK);
		assertTemplateRequest("222324", requestBody, 51L, HttpStatus.FORBIDDEN);
	}

	@Test
	void deleteTemplate() {
		assertDeleteTemplateRequest("user81", 51L, HttpStatus.FORBIDDEN);
		assertDeleteTemplateRequest("101112", 51L, HttpStatus.FORBIDDEN);
		assertDeleteTemplateRequest("222324", 51L, HttpStatus.FORBIDDEN);
		assertDeleteTemplateRequest("161718", 51L, HttpStatus.NOT_FOUND);
		assertDeleteTemplateRequest("192021", 52L, HttpStatus.OK);
		assertDeleteTemplateRequest("131415", 51L, HttpStatus.OK);
	}

	private void assertDeleteTemplateRequest(String token, Long shopId, HttpStatus expectedStatus) {
		HttpEntity<Object> request = getHttpEntity(token);
		ResponseEntity<String> res = template
				.exchange("/room?shop_id=" + shopId, HttpMethod.DELETE, request,
						String.class);
		assertEquals(expectedStatus, res.getStatusCode());
		if (expectedStatus == HttpStatus.OK) {
			Optional<RoomTemplateEntity> roomOptional = roomTemplateRepository.findByShopId(shopId);
			assertTrue(roomOptional.isEmpty());
		}
	}

	private void assertTemplateRequest(String token, String requestBody, Long shopId, HttpStatus expectedStatus) {
		HttpEntity<Object> request = getHttpEntity(requestBody, token);
		ResponseEntity<RoomResponse> res = template
				.postForEntity("/room/template?shop_id=" + shopId.toString(), request,
						RoomResponse.class);
		assertEquals(expectedStatus, res.getStatusCode());
		if (expectedStatus == HttpStatus.OK) {
			RoomResponse body = res.getBody();
			assertRoomResponse(body);
		}
	}

	private void assertRoomResponse(RoomResponse response) {
		assertRoomResponse(response, null, null, null, null);
	}

	private void assertRoomResponse(RoomResponse response, LocalDateTime beforeRequest, LocalDateTime afterRequest,
			String externalSessionId, String userEmail) {
		RoomTemplateEntity template = roomTemplateRepository.findByShopId(response.getShop().getId()).get();
		RoomResponse dbRoom = mapper.toRoomResponse(template);
		LocalDateTime dbTime = dbRoom.getSessionCreatedAt();
		dbRoom.setSessionCreatedAt(null);
		LocalDateTime responseTime = response.getSessionCreatedAt();
		response.setSessionCreatedAt(null);
		assertEquals(dbRoom, response);

		if (Objects.nonNull(beforeRequest) && Objects.nonNull(afterRequest)) {
			assertThat(dbTime).isBetween(beforeRequest, afterRequest);
			assertThat(responseTime).isBetween(beforeRequest, afterRequest);
		}

		if (Objects.nonNull(userEmail)) {
			assertEquals(externalSessionId, response.getSessionExternalId());
		}

		if (Objects.nonNull(userEmail)) {
			String email = template.getSession().getUserCreator().getEmail();
			assertEquals(userEmail, email);
		}
	}
}
