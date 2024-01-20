package com.nasnav.test;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import com.nasnav.dto.VideoChatLogRepresentationObject;
import com.nasnav.response.VideoChatListResponse;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "/sql/Video_chat_test_data.sql" })
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class VideoChatTest extends AbstractTestWithTempBaseDir {

	@Autowired
	private TestRestTemplate template;

	private static Stream<Arguments> generator() {
		String from = OffsetDateTime.now().minusDays(3).format(DateTimeFormatter.ISO_DATE_TIME);
		String to = OffsetDateTime.now().plusDays(7).format(DateTimeFormatter.ISO_DATE_TIME);

		return Stream.of(
				Arguments.of("875488", emptyMap(), Set.of(1L, 2L, 5L), 3, 3),
				Arguments.of("875488", Map.of("org_id", "99001"), Set.of(1L, 2L, 5L), 3, 3),
				Arguments.of("875488", Map.of("is_active", "true"), Set.of(1L, 5L), 2, 2),
				Arguments.of("875488", Map.of("shop_id", "502"), Set.of(2L), 1, 1),
				Arguments.of("875488", Map.of("has_shop", "false"), Set.of(1L, 5L), 2, 2),
				Arguments.of("875488", Map.of("is_assigned", "true"), Set.of(1L, 2L), 2, 2),
				Arguments.of("875488", Map.of("employee_id", "159"), Set.of(1L), 1, 1),
				Arguments.of("875488", Map.of("user_id", "88003"), Set.of(2L), 1, 1),
				Arguments.of("875488", Map.of("employee_id", "159"), Set.of(1L), 1, 1),
				Arguments.of("875488", Map.of("user_id", "88003"), Set.of(2L), 1, 1),
				Arguments.of("875488", Map.of("employee_id", "159"), Set.of(1L), 1, 1),
				Arguments.of("875488", Map.of("from", from, "to", to), Set.of(1L, 2L), 2, 2),
				Arguments.of("875488", Map.of("start", 0, "count", 2), Set.of(1L, 2L, 5L), 3, 2),
				Arguments.of("192021", emptyMap(), Set.of(2L), 1, 1),
				Arguments.of("192021", Map.of("org_id", "99001"), Set.of(2L), 1, 1),
				Arguments.of("192021", Map.of("shop_id", "502"), Set.of(2L), 1, 1));
	}

	@ParameterizedTest
	@MethodSource("generator")
	void testPasswordShouldBeReset(String token, Map<String, ?> query, Set<Long> expectedIds, int total, int currentSize) {
		HttpEntity<?> request = getHttpEntity(token);
		String queryParams = "?"
				+ query.keySet().stream().map(s -> s + "={" + s + "}").collect(Collectors.joining("&"));
		ResponseEntity<VideoChatListResponse> response = template.exchange("/videochat/sessions" + queryParams, GET, request,
				VideoChatListResponse.class, query);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		VideoChatListResponse body = response.getBody();
		assertEquals(total, body.getTotal());


		Set<Long> foundIds = body.getSessions().stream().map(VideoChatLogRepresentationObject::getId).collect(Collectors.toSet());

		assertEquals(currentSize, foundIds.size());
		assertTrue(expectedIds.containsAll(foundIds));
	}
}
