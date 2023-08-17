package com.nasnav.yeshtery.test.controllers.room;

import com.nasnav.dao.EventRoomTemplateRepository;
import com.nasnav.dto.response.EventRoomResponse;
import com.nasnav.mappers.EventRoomMapper;
import com.nasnav.persistence.EventRoomTemplateEntity;
import com.nasnav.yeshtery.test.commons.TestCommons.ParseablePage;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static com.nasnav.yeshtery.test.commons.TestCommons.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/Event_Room_API_Test_Data.sql")
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/database_cleanup.sql")
@NotThreadSafe
class EventRoomsApiTest extends AbstractTestWithTempBaseDir {
	private static Pageable DEFAULT_PAGINATION = PageRequest.of(0, 10, Sort.by("id"));

	@Autowired
	private TestRestTemplate template;
	@Autowired
	private EventRoomMapper mapper;
	@Autowired
	private EventRoomTemplateRepository roomTemplateRepository;

	@Test
	void getRoomsByUserToken() {
		Page<EventRoomResponse> rooms = roomTemplateRepository.findAllByEventOrganizationYeshteryStateEquals1(DEFAULT_PAGINATION)
				.map(mapper::toResponse);
		assertUserRooms("user81", rooms);
		assertUserRooms("user83", Page.empty());
	}

	private void assertUserRooms(String userToken, Page<EventRoomResponse> rooms) {

		HttpEntity<Object> request = getHttpEntity(userToken);
		ResponseEntity<ParseablePage<EventRoomResponse>> res = template
				.exchange("/v1/room/event/list_for_user", HttpMethod.GET, request,
						new ParameterizedTypeReference<ParseablePage<EventRoomResponse>>() {
						});
		assertEquals(HttpStatus.OK, res.getStatusCode());
		ParseablePage<EventRoomResponse> body = res.getBody();
		body.getContent().forEach(rt -> {
			rt.setCanStart(false);
			rt.setEvent(null);
		});
		assertEquals(rooms.getContent(), body.getContent());
	}

	@Test
	void getRoomsByOrgId() {
		assertOrgRooms(99001L, HttpStatus.OK);
		assertOrgRooms(99002L, HttpStatus.OK);
		assertOrgRooms(99003L, HttpStatus.NOT_FOUND);
	}

	void assertOrgRooms(Long orgId, HttpStatus status) {
		HttpEntity<Object> request = getHttpEntity(null);
		ResponseEntity<ParseablePage<EventRoomResponse>> res = template
				.exchange("/v1/room/event/list?org_id={orgId}", HttpMethod.GET, request,
						new ParameterizedTypeReference<ParseablePage<EventRoomResponse>>() {
						}, orgId);
		assertEquals(status, res.getStatusCode());

		if (status == HttpStatus.OK) {
			var body = res.getBody();
			body.getContent().forEach(rt -> {
				rt.setCanStart(false);
				rt.setEvent(null);
			});
			Page<EventRoomResponse> rooms = roomTemplateRepository
					.findAllByEventOrganizationId(orgId, DEFAULT_PAGINATION)
					.map(mapper::toResponse);
			assertEquals(rooms.getContent(), body.getContent());
		}
	}

	@Test
	void getSingleRoom() {
		EventRoomResponse room = roomTemplateRepository.findById(501L)
				.map(mapper::toResponse).get();
		ResponseEntity<EventRoomResponse> res = template
				.getForEntity("/v1/room/event?event_id=51",
						EventRoomResponse.class);
		assertEquals(HttpStatus.OK, res.getStatusCode());
		EventRoomResponse body = res.getBody();
		body.setCanStart(false);
		body.setEvent(null);
		assertEquals(room, body);
	}

	@Test
	void getSingleRoomNotYeshtery() {
		ResponseEntity<EventRoomResponse> res = template
				.getForEntity("/v1/room/event?event_id=54",
						EventRoomResponse.class);
		assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
	}

	@Test
	void setRoomSession() {
		HttpEntity<Object> request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user81");
		LocalDateTime before = LocalDateTime.now();
		ResponseEntity<EventRoomResponse> res = template
				.postForEntity("/v1/room/event/session?event_id=51", request,
						EventRoomResponse.class);
		LocalDateTime after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		EventRoomResponse body = res.getBody();
		assertRoomResponse(body, before, after, "test_new_session", "user81@nasnav.com");

		request = getHttpEntity("{\"session_external_id\":\"test_new_session_2\"}", "user81");
		before = LocalDateTime.now();
		res = template
				.postForEntity("/v1/room/event/session?event_id=52", request,
						EventRoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user82");
		ResponseEntity<String> res2 = template
				.postForEntity("/v1/room/event/session?event_id=51", request,
						String.class);
		assertEquals(HttpStatus.NOT_FOUND, res2.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user82");
		res2 = template
				.postForEntity("/v1/room/event/session?event_id=53", request,
						String.class);
		assertEquals(HttpStatus.NOT_FOUND, res2.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user83");
		before = LocalDateTime.now();
		res = template
				.postForEntity("/v1/room/event/session?event_id=52", request,
						EventRoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		body = res.getBody();
		assertRoomResponse(body, before, after, "test_new_session", "user83@nasnav.com");

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "131415");
		before = LocalDateTime.now();
		res = template
				.postForEntity("/v1/room/event/session?event_id=52", request,
						EventRoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		body = res.getBody();
		assertRoomResponse(body, before, after, "test_new_session", null);
	}

	@Test
	void setRoomSessionWithoutBody() {
		HttpEntity<Object> request = getHttpEntity("user83");
		LocalDateTime before = LocalDateTime.now();
		ResponseEntity<EventRoomResponse> res = template
				.postForEntity("/v1/room/event/session?event_id=52", request,
						EventRoomResponse.class);
		LocalDateTime after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		EventRoomResponse body = res.getBody();
		String externalId = body.getSessionExternalId();
		assertRoomResponse(body, before, after, externalId, "user83@nasnav.com");
		assertDoesNotThrow(() -> UUID.fromString(externalId));

		before = LocalDateTime.now();
		res = template
				.postForEntity("/v1/room/event/session?event_id=52", request,
						EventRoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		body = res.getBody();
		assertRoomResponse(body, before, after, externalId, "user83@nasnav.com");
		assertDoesNotThrow(() -> UUID.fromString(externalId));
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

	private void assertDeleteTemplateRequest(String token, Long eventId, HttpStatus expectedStatus) {
		HttpEntity<Object> request = getHttpEntity(token);
		ResponseEntity<String> res = template
				.exchange("/v1/room/event?event_id=" + eventId, HttpMethod.DELETE, request,
						String.class);
		assertEquals(expectedStatus, res.getStatusCode());
		if (expectedStatus == HttpStatus.OK) {
			Optional<EventRoomTemplateEntity> roomOptional = roomTemplateRepository.findByEventId(eventId);
			assertTrue(roomOptional.isEmpty());
		}
	}

	private void assertTemplateRequest(String token, String requestBody, Long eventId, HttpStatus expectedStatus) {
		HttpEntity<Object> request = getHttpEntity(requestBody, token);
		ResponseEntity<EventRoomResponse> res = template
				.postForEntity("/v1/room/event/template?event_id=" + eventId.toString(), request,
						EventRoomResponse.class);
		assertEquals(expectedStatus, res.getStatusCode());
		if (expectedStatus == HttpStatus.OK) {
			EventRoomResponse body = res.getBody();
			assertRoomResponse(body);
		}
	}

	private void assertRoomResponse(EventRoomResponse response) {
		assertRoomResponse(response, null, null, null, null);
	}

	private void assertRoomResponse(EventRoomResponse response, LocalDateTime beforeRequest, LocalDateTime afterRequest,
			String externalSessionId, String userEmail) {
		EventRoomTemplateEntity template = roomTemplateRepository.findByEventId(response.getEvent().getId()).get();
		EventRoomResponse dbRoom = mapper.toResponse(template);
		LocalDateTime dbTime = dbRoom.getSessionCreatedAt();
		dbRoom.setSessionCreatedAt(null);
		LocalDateTime responseTime = response.getSessionCreatedAt();
		response.setSessionCreatedAt(null);
		response.setCanStart(false);
		response.setEvent(null);
		assertEquals(dbRoom, response);

		if (Objects.nonNull(beforeRequest) && Objects.nonNull(afterRequest)) {
			assertThat(dbTime).isBetween(beforeRequest, afterRequest);
			assertThat(responseTime).isBetween(beforeRequest, afterRequest);
		}

		if (Objects.nonNull(externalSessionId)) {
			assertEquals(externalSessionId, response.getSessionExternalId());
		}

		if (Objects.nonNull(userEmail)) {
			String email = template.getSession().getUserCreator().getEmail();
			assertEquals(userEmail, email);
		}
	}
}
