package com.nasnav.dto.rocketchat;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RocketChatUserDTO implements RocketChatWrappedData {
	private String id;
	private String name;
	private String username;
	private String email;
	private boolean active;

	@Override
	public String getFieldName() {
		return "user";
	}

	public void setVisitorEmails(List<Map<String, String>> guestEmails) {
		email = guestEmails.get(0).get("address");
	}

	@JsonProperty("_id")
	public void setId(String id) {
		this.id = id;
	}
}
