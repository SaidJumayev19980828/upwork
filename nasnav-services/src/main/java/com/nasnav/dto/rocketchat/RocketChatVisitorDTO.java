package com.nasnav.dto.rocketchat;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RocketChatVisitorDTO implements RocketChatWrappedData {

	private String id;

	private String name;

	private String email;

	private String token;

	private String department;

	@JsonIgnore
	private String userId;

	@JsonIgnore
	private String orgId;

	public void setVisitorEmails(List<Map<String, String>> guestEmails) {
		email = guestEmails.get(0).get("address");
	}

	@JsonProperty("_id")
	public void setId(String id) {
		this.id = id;
	}

	public List<RocketChatCustomFieldDTO> getCustomFields() {
		return List.of(new RocketChatCustomFieldDTO("user_id", userId),
				new RocketChatCustomFieldDTO("org_id", orgId));
	}

	public void setCustomFields(List<RocketChatCustomFieldDTO> cutomField) {
		cutomField.forEach(field -> {
			if ("user_id".equals(field.getKey())) {
				setUserId(field.getValue());
			}
			if ("org_id".equals(field.getKey())) {
				setOrgId(field.getValue());
			}
		});
	}

	@Override
	public String getFieldName() {
		return "visitor";
	}
}
