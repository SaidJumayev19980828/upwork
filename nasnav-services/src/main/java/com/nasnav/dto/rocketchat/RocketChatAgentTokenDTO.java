package com.nasnav.dto.rocketchat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RocketChatAgentTokenDTO implements RocketChatWrappedData {
	private String userId;
	private String username;
	private String authToken;

	@Override
	public String getFieldName() {
		return "data";
	}
	
}
