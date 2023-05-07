package com.nasnav.dto.rocketchat;

import lombok.Data;

@Data
public class RocketChatConfigDTO implements RocketChatWrappedData {
	RocketChatVisitorDTO guest;

	@Override
	public String getFieldName() {
		return "config";
	}
}
