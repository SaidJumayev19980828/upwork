package com.nasnav.dto.rocketchat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RocketChatDepartmentDTO implements RocketChatWrappedData {
	@JsonProperty("_id")
	private String id;
	private String name;
	private String email;
	private String description;
	private boolean enabled;
	@Builder.Default
	private boolean showOnRegistration = false;
	@Builder.Default
	private boolean showOnOfflineForm = false;

	@Override
	public String getFieldName() {
		return "department";
	}

}