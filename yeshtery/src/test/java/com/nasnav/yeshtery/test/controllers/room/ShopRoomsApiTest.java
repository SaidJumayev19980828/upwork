package com.nasnav.yeshtery.test.controllers.room;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nasnav.dao.ShopRatingRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.ShopRateDTO;
import com.nasnav.persistence.ShopRating;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.shipping.services.mylerz.webclient.dto.Shop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import com.nasnav.dao.ShopRoomTemplateRepository;
import com.nasnav.dto.response.ShopRoomResponse;
import com.nasnav.mappers.ShopRoomMapper;
import com.nasnav.persistence.ShopRoomTemplateEntity;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;

@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/Shop_Room_Api_Test_Data.sql")
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/database_cleanup.sql")
@NotThreadSafe
class ShopRoomsApiTest extends AbstractTestWithTempBaseDir {

	@Autowired
	private TestRestTemplate template;
	@Autowired
	private ShopRoomMapper mapper;
	@Autowired
	private ShopRoomTemplateRepository roomTemplateRepository;
	@Mock
	private ShopRatingRepository shopRatingRepository;
	@Mock
	private ShopsRepository shopsRepository;
	@Mock
	private UserEntity user;
	@Mock
	private ShopsEntity shops;


	@Test
	void getRoomsByUserToken() {
		Set<ShopRoomResponse> rooms = roomTemplateRepository.findAllByShopOrganizationEntityYeshteryStateEquals1()
				.stream()
				.map(mapper::toResponse).collect(Collectors.toSet());
		assertUserRooms("user81", rooms);
		assertUserRooms("user83", Set.of());
	}

	@Test
	 void RateReviewShopSuccessfully() {
		ShopRateDTO requestBody = new ShopRateDTO(   51L,5,"good");
		HttpCookie cookie = new HttpCookie("User-Token", "user81");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cookie", cookie.toString());
		headers.add("User-Token", "user81");
		HttpEntity<ShopRateDTO> request = new HttpEntity<>(requestBody,headers);

		ResponseEntity<Void> response =
				template.exchange("/v1/room/shop/rateShop", POST, request, Void.class);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	private void assertUserRooms(String userToken, Set<ShopRoomResponse> rooms) {

		HttpEntity<Object> request = getHttpEntity(userToken);
		ResponseEntity<Set<ShopRoomResponse>> res = template
				.exchange("/v1/room/shop/list_for_user", HttpMethod.GET, request,
						new ParameterizedTypeReference<Set<ShopRoomResponse>>() {
						});
		assertEquals(HttpStatus.OK, res.getStatusCode());
		Set<ShopRoomResponse> body = res.getBody();
		assertEquals(rooms, body);
	}

	@Test
	void getRoomsByOrgId() {
		ParameterizedTypeReference<Set<ShopRoomResponse>> okResponseType = new ParameterizedTypeReference<Set<ShopRoomResponse>>() {
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
				.exchange("/v1/room/shop/list?org_id={orgId}", HttpMethod.GET, request, type, orgId);
		assertEquals(status, res.getStatusCode());

		if (status == HttpStatus.OK) {
			var body = res.getBody();
			Set<ShopRoomResponse> rooms = roomTemplateRepository.findAllByShopOrganizationEntityId(orgId)
					.stream()
					.map(mapper::toResponse).collect(Collectors.toSet());
			assertEquals(rooms, body);
		}
	}

	@Test
	void getSingleRoom() {
		ShopRoomResponse room = roomTemplateRepository.findById(501L)
				.map(mapper::toResponse).get();
		ResponseEntity<ShopRoomResponse> res = template
				.getForEntity("/v1/room/shop?shop_id=51",
						ShopRoomResponse.class);
		assertEquals(HttpStatus.OK, res.getStatusCode());
		ShopRoomResponse body = res.getBody();
		assertEquals(room, body);
	}

	@Test
	void getSingleRoomNotYeshtery() {
		ResponseEntity<ShopRoomResponse> res = template
				.getForEntity("/v1/room/shop?shop_id=54",
						ShopRoomResponse.class);
		assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
	}

	@Test
	void setRoomSession() {
		HttpEntity<Object> request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user81");
		LocalDateTime before = LocalDateTime.now();
		ResponseEntity<ShopRoomResponse> res = template
				.postForEntity("/v1/room/shop/session?shop_id=51", request,
						ShopRoomResponse.class);
		LocalDateTime after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		ShopRoomResponse body = res.getBody();
		assertRoomResponse(body, before, after, "test_new_session", "user81@nasnav.com");

		request = getHttpEntity("{\"session_external_id\":\"test_new_session_2\"}", "user81");
		before = LocalDateTime.now();
		res = template
				.postForEntity("/v1/room/shop/session?shop_id=52", request,
						ShopRoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		body = res.getBody();
		assertRoomResponse(body, before, after, "test_new_session_2", "user81@nasnav.com");

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user82");
		res = template
				.postForEntity("/v1/room/shop/session?shop_id=51", request,
						ShopRoomResponse.class);
		assertEquals(HttpStatus.OK, res.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user82");
		res = template
				.postForEntity("/v1/room/shop/session?shop_id=54", request,
						ShopRoomResponse.class);
		assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());

		request = getHttpEntity("{\"session_external_id\":\"test_new_session\"}", "user83");
		res = template
				.postForEntity("/v1/room/shop/session?shop_id=54", request,
						ShopRoomResponse.class);
		assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
	}

	@Test
	void setRoomSessionWithoutBody() {
		HttpEntity<Object> request = getHttpEntity("user81");
		LocalDateTime before = LocalDateTime.now();
		ResponseEntity<ShopRoomResponse> res = template
				.postForEntity("/v1/room/shop/session?shop_id=52", request,
						ShopRoomResponse.class);
		LocalDateTime after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		ShopRoomResponse body = res.getBody();
		String externalId = body.getSessionExternalId();
		assertRoomResponse(body, before, after, externalId, "user81@nasnav.com");
		assertDoesNotThrow(() -> UUID.fromString(externalId));

		before = LocalDateTime.now();
		res = template
				.postForEntity("/v1/room/shop/session?shop_id=52", request,
						ShopRoomResponse.class);
		after = LocalDateTime.now();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		body = res.getBody();
		assertRoomResponse(body, before, after, externalId, "user81@nasnav.com");
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

	private void assertDeleteTemplateRequest(String token, Long shopId, HttpStatus expectedStatus) {
		HttpEntity<Object> request = getHttpEntity(token);
		ResponseEntity<String> res = template
				.exchange("/v1/room/shop?shop_id=" + shopId, HttpMethod.DELETE, request,
						String.class);
		assertEquals(expectedStatus, res.getStatusCode());
		if (expectedStatus == HttpStatus.OK) {
			Optional<ShopRoomTemplateEntity> roomOptional = roomTemplateRepository.findByShopId(shopId);
			assertTrue(roomOptional.isEmpty());
		}
	}

	private void assertTemplateRequest(String token, String requestBody, Long shopId, HttpStatus expectedStatus) {
		HttpEntity<Object> request = getHttpEntity(requestBody, token);
		ResponseEntity<ShopRoomResponse> res = template
				.postForEntity("/v1/room/shop/template?shop_id=" + shopId.toString(), request,
						ShopRoomResponse.class);
		assertEquals(expectedStatus, res.getStatusCode());
		if (expectedStatus == HttpStatus.OK) {
			ShopRoomResponse body = res.getBody();
			assertRoomResponse(body);
		}
	}

	private void assertRoomResponse(ShopRoomResponse response) {
		assertRoomResponse(response, null, null, null, null);
	}

	private void assertRoomResponse(ShopRoomResponse response, LocalDateTime beforeRequest, LocalDateTime afterRequest,
			String externalSessionId, String userEmail) {
		ShopRoomTemplateEntity template = roomTemplateRepository.findByShopId(response.getShop().getId()).get();
		ShopRoomResponse dbRoom = mapper.toResponse(template);
		LocalDateTime dbTime = dbRoom.getSessionCreatedAt();
		dbRoom.setSessionCreatedAt(null);
		LocalDateTime responseTime = response.getSessionCreatedAt();
		response.setSessionCreatedAt(null);
		assertEquals(dbRoom, response);

		if (Objects.nonNull(beforeRequest) && Objects.nonNull(afterRequest)) {
			LocalDateTime tolerentBefore = beforeRequest.minusSeconds(1);
			LocalDateTime tolerentAfter = afterRequest.plusSeconds(1);
			assertThat(dbTime).isBetween(tolerentBefore, tolerentAfter);
			assertThat(responseTime).isBetween(tolerentBefore, tolerentAfter);
		}

		if (Objects.nonNull(userEmail)) {
			assertEquals(externalSessionId, response.getSessionExternalId());
		}

		if (Objects.nonNull(userEmail)) {
			String email = template.getSession().getUserCreator().getEmail();
			assertEquals(userEmail, email);
		}
	}


	@Test
	void rateShopServiceTest(){
		ShopRateDTO shopRateDTO= new ShopRateDTO(501L,5,"good");
		Optional<ShopsEntity> shopsEntity=shopsRepository.findById(shopRateDTO.getShopId());
		Assertions.assertNotNull(shopsEntity);
	}

	@Test
	 void createShopRateServiceTest(){
		ShopRateDTO dto= new ShopRateDTO(502L,5,"good");
		ShopRating rate = new ShopRating();
		rate.setRate(dto.getRate());
		rate.setId(2L);
		rate.setShop(shops);
		rate.setReview(dto.getReview());
		rate.setUser(user);
		rate.setApproved(false);
		shopRatingRepository.save(rate);
		Assertions.assertNotNull(shopRatingRepository.findById(rate.getId()));
	}
}
