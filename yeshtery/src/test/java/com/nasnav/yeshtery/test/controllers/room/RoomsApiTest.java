package com.nasnav.yeshtery.test.controllers.room;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import com.nasnav.dao.RoomTemplateRepository;
import com.nasnav.dto.response.RoomResponse;
import com.nasnav.mappers.RoomMapper;
import com.nasnav.persistence.RoomTemplateEntity;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;

@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/Room_Api_Test_Data.sql")
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/database_cleanup.sql")
@NotThreadSafe
class RoomsApiTest extends AbstractTestWithTempBaseDir {

	@Autowired
	private TestRestTemplate template;
	@Autowired
	private RoomMapper mapper;
	@Autowired
	private RoomTemplateRepository roomTemplateRepository;

	@Test
	void getRoomsByUserToken() {
		Set<RoomResponse> rooms = roomTemplateRepository.findAllByShopOrganizationEntityYeshteryStateEquals1()
				.stream()
				.map(mapper::toRoomResponse).collect(Collectors.toSet());
		assertUserRooms("user81", rooms);
		assertUserRooms("user83", Set.of());
	}

	private void assertUserRooms(String userToken, Set<RoomResponse> rooms) {

		HttpEntity<Object> request = getHttpEntity(userToken);
		ResponseEntity<Set<RoomResponse>> res = template
				.exchange("/v1/room/list_for_user", HttpMethod.GET, request,
						new ParameterizedTypeReference<Set<RoomResponse>>() {
						});
		assertEquals(HttpStatus.OK, res.getStatusCode());
		Set<RoomResponse> body = res.getBody();
		assertEquals(rooms, body);
	}

	@Test
	void getRoomsByOrgId() {
		ParameterizedTypeReference<Set<RoomResponse>> okResponseType = new ParameterizedTypeReference<Set<RoomResponse>>() {
		};
		ParameterizedTypeReference<String> failResponseType = new ParameterizedTypeReference<String>() {
		};
		assertOrgRooms(99001L, HttpStatus.OK, okResponseType);
		assertOrgRooms(99002L, HttpStatus.OK, okResponseType);
		assertOrgRooms(99003L, HttpStatus.NOT_FOUND, failResponseType);
	}

	void assertOrgRooms(Long orgId, HttpStatus status, ParameterizedTypeReference<?> type) {
		HttpEntity<Object> request = getHttpEntity(null);
		ResponseEntity<?> res = template
				.exchange("/v1/room/list?org_id={orgId}", HttpMethod.GET, request, type, orgId);
		assertEquals(status, res.getStatusCode());

		if (status == HttpStatus.OK) {
			var body = res.getBody();
			Set<RoomResponse> rooms = roomTemplateRepository.findAllByShopOrganizationEntityId(orgId)
					.stream()
					.map(mapper::toRoomResponse).collect(Collectors.toSet());
			assertEquals(rooms, body);
		}
	}

	@Test
	void getSingleRoom() {
		RoomResponse room = roomTemplateRepository.findById(501L)
				.map(mapper::toRoomResponse).get();
		ResponseEntity<RoomResponse> res = template
				.getForEntity("/v1/room?shop_id=51",
						RoomResponse.class);
		assertEquals(HttpStatus.OK, res.getStatusCode());
		RoomResponse body = res.getBody();
		assertEquals(room, body);
	}

	@Test
	void getSingleRoomNotYeshtery() {
		ResponseEntity<RoomResponse> res = template
				.getForEntity("/v1/room?shop_id=54",
						RoomResponse.class);
		assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
	}

	@Test
	void setRoomSession() {
		HttpEntity<Object> request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user81");
		LocalDateTime before = LocalDateTime.now();
		ResponseEntity<RoomResponse> res = template
				.postForEntity("/v1/room/session?shop_id=51", request,
						RoomResponse.class);
		LocalDateTime after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		RoomResponse body = res.getBody();
		assertRoomResponse(body, before, after, "test_new_session", "user81@nasnav.com");

		request = getHttpEntity("{\"session_external_id\":\"test_new_session_2\"}", "user81");
		before = LocalDateTime.now();
		res = template
				.postForEntity("/v1/room/session?shop_id=52", request,
						RoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		body = res.getBody();
		assertRoomResponse(body, before, after, "test_new_session_2", "user81@nasnav.com");

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user82");
		res = template
				.postForEntity("/v1/room/session?shop_id=51", request,
						RoomResponse.class);
		assertEquals(HttpStatus.OK, res.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user82");
		res = template
				.postForEntity("/v1/room/session?shop_id=54", request,
						RoomResponse.class);
		assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user83");
		res = template
				.postForEntity("/v1/room/session?shop_id=54", request,
						RoomResponse.class);
		assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
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
				.exchange("/v1/room?shop_id=" + shopId, HttpMethod.DELETE, request,
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
				.postForEntity("/v1/room/template?shop_id=" + shopId.toString(), request,
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
