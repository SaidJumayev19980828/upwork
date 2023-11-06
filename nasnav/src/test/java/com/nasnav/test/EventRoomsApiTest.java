package com.nasnav.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.dao.EventRoomTemplateRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.EventsNewDTO;
import com.nasnav.dto.EventsRoomNewDTO;
import com.nasnav.dto.response.EventRoomResponse;
import com.nasnav.dto.response.PostResponseDTO;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.enumerations.EventRoomStatus;
import com.nasnav.mappers.EventRoomMapper;
import com.nasnav.persistence.EventRoomTemplateEntity;
import com.nasnav.test.commons.TestCommons.ParseablePage;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static com.nasnav.test.commons.TestCommons.*;
import static com.nasnav.enumerations.EventRoomStatus.*;
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
	@Autowired
	private UserRepository userRepository;

	@Test
	void getRoomsByUserToken() throws JsonProcessingException {
		userRepository.findAll().forEach(user -> {
			Page<EventRoomResponse> rooms = roomTemplateRepository
					.findAllByEventOrganizationIdAndStatus(user.getOrganizationId(), STARTED, DEFAULT_PAGINATION)
					.map(mapper::toResponse);
			HttpEntity<Object> request = getHttpEntity(user.getAuthenticationToken());
			ResponseEntity<ParseablePage<EventRoomResponse>> res = template
					.exchange("/room/event/list_for_user?status=STARTED", HttpMethod.GET, request,
							new ParameterizedTypeReference<ParseablePage<EventRoomResponse>>() {
							});
			assertEquals(HttpStatus.OK, res.getStatusCode());
			ParseablePage<EventRoomResponse> body = res.getBody();
			body.getContent().forEach(rt -> {
				rt.setCanStart(false);
				rt.setEvent(null);
			});
			assertEquals(rooms.getContent(), body.getContent());
			assertTrue(body.getContent().stream().allMatch(room -> STARTED.equals(room.getStatus())));
		});
	}

	@Test
	void getRoomsByOrgIdAndFilters() {
		List.of(99001L, 99002L).forEach(orgId -> {
			List.of(NOT_STARTED, STARTED, SUSPENDED, ENDED).forEach(status -> {
				Page<EventRoomResponse> rooms = roomTemplateRepository
						.findAllByEventOrganizationIdAndStatus(orgId, status, DEFAULT_PAGINATION)
						.map(mapper::toResponse);
				HttpEntity<Object> request = getHttpEntity(null);
				ResponseEntity<ParseablePage<EventRoomResponse>> res = template
						.exchange("/room/event/list?org_id={orgId}&status={status}", HttpMethod.GET, request,
								new ParameterizedTypeReference<ParseablePage<EventRoomResponse>>() {
								}, orgId, status);
				assertEquals(HttpStatus.OK, res.getStatusCode());
				ParseablePage<EventRoomResponse> body = res.getBody();
				body.getContent().forEach(rt -> {
					rt.setCanStart(false);
					rt.setEvent(null);
				});
				assertEquals(rooms.getContent(), body.getContent());
				assertTrue(body.getContent().stream().allMatch(room -> status.equals(room.getStatus())));
			});
		});
	}

	@Test
	void getRoomsByOrgIdVerifyStatusRoles() {
		List.of(99001L, 99002L).forEach(orgId -> {
			Page<EventRoomResponse> rooms = roomTemplateRepository
					.findAllByEventOrganizationId(orgId, DEFAULT_PAGINATION)
					.map(mapper::toResponse);
			HttpEntity<Object> request = getHttpEntity(null);
			ResponseEntity<ParseablePage<EventRoomResponse>> res = template
					.exchange("/room/event/list?org_id={orgId}", HttpMethod.GET, request,
							new ParameterizedTypeReference<ParseablePage<EventRoomResponse>>() {
							}, orgId);
			assertEquals(HttpStatus.OK, res.getStatusCode());
			ParseablePage<EventRoomResponse> body = res.getBody();
			body.getContent().forEach(rt -> {
				rt.setCanStart(false);
				rt.setEvent(null);
			});
			assertEquals(rooms.getContent(), body.getContent());
			body.getContent().forEach(this::assertRoomSessionIdNotShownUnlessNeeded);
		});
	}

	@Test
	void getSingleRoom() {
		ResponseEntity<EventRoomResponse> res = template
				.getForEntity("/room/event?event_id=51",
						EventRoomResponse.class);
		assertEquals(HttpStatus.OK, res.getStatusCode());
		EventRoomResponse body = res.getBody();
		assertRoomResponse(body);
	}

	@Test
	void setRoomSession() {
		HttpEntity<Object> request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user81");
		LocalDateTime before = LocalDateTime.now();
		ResponseEntity<EventRoomResponse> res = template
				.postForEntity("/room/event/session?event_id=51", request,
						EventRoomResponse.class);
		LocalDateTime after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		EventRoomResponse body = res.getBody();
		Duration maxDuration = Duration.ofMinutes(2);
		Duration actualDuration = Duration.between(before, after);
		assertTrue(actualDuration.compareTo(maxDuration) <= 0, "Time duration exceeded the acceptable range");
		assertEquals(HttpStatus.OK, res.getStatusCode());

//		assertRoomResponse(body, before, after, "test_new_session", "user81@nasnav.com");

		request = getHttpEntity("{\"session_external_id\":\"test_new_session_2\"}", "user81");
		before = LocalDateTime.now();
		res = template
				.postForEntity("/room/event/session?event_id=52", request,
						EventRoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user82");
		ResponseEntity<String> res2 = template
				.postForEntity("/room/event/session?event_id=51", request,
						String.class);
		assertEquals(HttpStatus.NOT_FOUND, res2.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user82");
		res2 = template
				.postForEntity("/room/event/session?event_id=53", request,
						String.class);
		assertEquals(HttpStatus.NOT_FOUND, res2.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user83");
		before = LocalDateTime.now();
		res = template
				.postForEntity("/room/event/session?event_id=52", request,
						EventRoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		body = res.getBody();
		 actualDuration = Duration.between(before, after);
		assertTrue(actualDuration.compareTo(maxDuration) <= 0, "Time duration exceeded the acceptable range");
		assertEquals(HttpStatus.OK, res.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "131415");
		before = LocalDateTime.now();
		res = template
				.postForEntity("/room/event/session?event_id=52", request,
						EventRoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		body = res.getBody();
		actualDuration = Duration.between(before, after);
		assertTrue(actualDuration.compareTo(maxDuration) <= 0, "Time duration exceeded the acceptable range");
		assertEquals(HttpStatus.OK, res.getStatusCode());
	}

	@Test
	void setRoomSessionWithoutBody() {
		HttpEntity<Object> request = getHttpEntity("user83");
		LocalDateTime before = LocalDateTime.now();
		ResponseEntity<EventRoomResponse> res = template
				.postForEntity("/room/event/session?event_id=52", request,
						EventRoomResponse.class);
		LocalDateTime after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		EventRoomResponse body = res.getBody();
		String externalId = body.getSessionExternalId();
		assertRoomResponse(body, before, after, externalId, "user83@nasnav.com");
		assertDoesNotThrow(() -> UUID.fromString(externalId));

		before = LocalDateTime.now();
		res = template
				.postForEntity("/room/event/session?event_id=52", request,
						EventRoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		body = res.getBody();
		assertRoomResponse(body, before, after, externalId, "user83@nasnav.com");
		assertDoesNotThrow(() -> UUID.fromString(externalId));
	}

	@Test
	public void getEventRoomTest(){
		HttpEntity<Object> httpEntity = getHttpEntity("user81");
		var responseType = new ParameterizedTypeReference<>() {
		};

		var response = template.exchange("/room/event/all?orgId=99001&start=0&count=10" , HttpMethod.GET, httpEntity, responseType);
		assertEquals(200, response.getStatusCode().value());
	}

	@Test
	void testSuspendRoom() {
		assertSuspendRoom(51L, "131415", HttpStatus.OK);
		assertSuspendRoom(52L, "131415", HttpStatus.NOT_ACCEPTABLE);
		assertSuspendRoom(53L, "131415", HttpStatus.NOT_FOUND);
		assertSuspendRoom(54L, "131415", HttpStatus.NOT_ACCEPTABLE);
		assertSuspendRoom(55L, "131415", HttpStatus.OK);
	}

	private void assertSuspendRoom(Long eventId, String userToken, HttpStatus expectedHttpStatus) {
		HttpEntity<Object> request = getHttpEntity(userToken);
		ResponseEntity<Void> res = template
				.postForEntity("/room/event/session/suspend?event_id={eventId}", request,
						Void.class, eventId);
		assertEquals(expectedHttpStatus, res.getStatusCode());
		if (expectedHttpStatus == HttpStatus.OK) {
			EventRoomResponse roomResponse = template.getForObject("/room/event?event_id={eventId}",
					EventRoomResponse.class, eventId);
			assertRoomResponse(roomResponse, SUSPENDED);
		}

	}

	@Test
	void postTemplate() {
		String requestBody = "{\"scene_id\": \"someId\", \"data\": \"some data\"}";
		assertTemplatePostRequest("user81", requestBody, 51L, HttpStatus.FORBIDDEN);
		assertTemplatePostRequest("101112", requestBody, 51L, HttpStatus.FORBIDDEN);
		assertTemplatePostRequest("131415", requestBody, 51L, HttpStatus.OK);
		assertTemplatePostRequest("161718", requestBody, 51L, HttpStatus.NOT_FOUND);
		assertTemplatePostRequest("192021", requestBody, 52L, HttpStatus.OK);
		assertTemplatePostRequest("222324", requestBody, 51L, HttpStatus.FORBIDDEN);
		assertTemplatePostRequest("161718", requestBody, 53L, HttpStatus.OK);
	}

	@Test
	void postTemplateOtherOrg() {
		String requestBody = "{\"scene_id\": \"someId\", \"data\": \"some data\"}";
		assertTemplatePostRequest("131415", requestBody, 53L, HttpStatus.NOT_FOUND);
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
				.exchange("/room/event?event_id=" + eventId, HttpMethod.DELETE, request,
						String.class);
		assertEquals(expectedStatus, res.getStatusCode());
		if (expectedStatus == HttpStatus.OK) {
			Optional<EventRoomTemplateEntity> roomOptional = roomTemplateRepository.findByEventId(eventId);
			assertTrue(roomOptional.isEmpty());
		}
	}

	private void assertTemplatePostRequest(String token, String requestBody, Long eventId,
			HttpStatus expectedHttpStatus) {
		HttpEntity<Object> request = getHttpEntity(requestBody, token);
		ResponseEntity<EventRoomResponse> res = template
				.postForEntity("/room/event/template?event_id=" + eventId.toString(), request,
						EventRoomResponse.class);
		assertEquals(expectedHttpStatus, res.getStatusCode());
		if (expectedHttpStatus == HttpStatus.OK) {
			EventRoomResponse body = res.getBody();
			assertRoomResponse(body);
		}
	}

	private void assertRoomResponse(EventRoomResponse response) {
		assertRoomResponse(response, null);
	}

	private void assertRoomResponse(EventRoomResponse response, EventRoomStatus roomStatus) {
		assertRoomResponse(response, null, null, null, null, roomStatus);
	}

	private void assertRoomResponse(EventRoomResponse response, LocalDateTime beforeRequest, LocalDateTime afterRequest,
			String externalSessionId, String userEmail) {
		assertRoomResponse(response, beforeRequest, afterRequest, externalSessionId, userEmail, null);
	}

	private void assertRoomResponse(EventRoomResponse response, LocalDateTime beforeRequest, LocalDateTime afterRequest,
			String externalSessionId, String userEmail, EventRoomStatus roomStatus) {
		EventRoomTemplateEntity template = roomTemplateRepository.findByEventId(response.getEvent().getId()).get();
		EventRoomResponse dbRoom = mapper.toResponse(template);
		LocalDateTime dbTime = dbRoom.getSessionCreatedAt();
		dbRoom.setSessionCreatedAt(null);
		LocalDateTime responseTime = response.getSessionCreatedAt();
		response.setSessionCreatedAt(null);
		response.setCanStart(false);
		response.setEvent(null);
		assertEquals(dbRoom, response);

		assertNotNull(response.getStatus());

		assertRoomSessionIdNotShownUnlessNeeded(response);

		if (Objects.nonNull(beforeRequest) && Objects.nonNull(afterRequest)) {
			LocalDateTime tolerentBefore = beforeRequest.minusSeconds(1);
			LocalDateTime tolerentAfter = afterRequest.plusSeconds(1);
			assertThat(dbTime).isBetween(tolerentBefore, tolerentAfter);
			assertThat(responseTime).isBetween(tolerentBefore, tolerentAfter);
		}

		if (Objects.nonNull(externalSessionId)) {
			assertEquals(externalSessionId, response.getSessionExternalId());
		}

		if (Objects.nonNull(userEmail)) {
			String email = template.getSession().getUserCreator().getEmail();
			assertEquals(userEmail, email);
		}

		if (roomStatus != null) {
			assertEquals(roomStatus, response.getStatus());
		}
	}

	private void assertRoomSessionIdNotShownUnlessNeeded(EventRoomResponse response) {
		assertTrue(STARTED.equals(response.getStatus()) || response.isCanStart()
				|| response.getSessionExternalId() == null);
	}
}
