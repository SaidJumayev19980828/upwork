package com.nasnav.dto.rocketchat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RocketChatDepartmentDTO implements RocketChatWrappedData {
	private String id;
	private String name;
	private String email;
	private String description;
	private boolean enabled;

	@JsonProperty("_id")
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getFieldName() {
		return "department";
	}

}