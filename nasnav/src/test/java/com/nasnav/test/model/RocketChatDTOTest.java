package com.nasnav.test.model;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.rocketchat.RocketChatConfigDTO;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.dto.rocketchat.RocketChatResponseWrapper;

class RocketChatDTOTest {

	ObjectMapper mapper = new ObjectMapper();

	@Test
	void parseGuestJSON() throws IOException {
		String json = "{\"_id\":\"testId\",\"username\":\"guest-11\",\"name\":\"testName\",\"token\":\"testToken\",\"visitorEmails\":[{\"address\":\"test@email.co\"}]}";
		RocketChatVisitorDTO guest = mapper.readValue(json, RocketChatVisitorDTO.class);
		assertEquals("testName", guest.getName());
		assertEquals("testId", guest.getId());
		assertEquals("test@email.co", guest.getEmail());
		assertEquals("testToken", guest.getToken());
	}

	@Test
	void parseWrappedConfigJSON() throws IOException {
		JavaType wrappedConfigType = mapper.getTypeFactory().constructParametricType(RocketChatResponseWrapper.class,
				RocketChatConfigDTO.class);
		RocketChatResponseWrapper<RocketChatConfigDTO> configWrapper = mapper
				.readValue(new File("src/test/resources/json/rocket_chat/config_response.json"), wrappedConfigType);

		// serialize then deserialize to check both
		configWrapper = mapper.convertValue(configWrapper, wrappedConfigType);

		assertTrue(configWrapper.getSuccess());
		RocketChatVisitorDTO guest = configWrapper.getData().getGuest();
		assertEquals("testName", guest.getName());
		assertEquals("testId", guest.getId());
		assertEquals("test@email.co", guest.getEmail());
		assertEquals("testToken", guest.getToken());
	}
}