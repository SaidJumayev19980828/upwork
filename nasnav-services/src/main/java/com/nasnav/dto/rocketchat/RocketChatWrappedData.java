package com.nasnav.dto.rocketchat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface RocketChatWrappedData {
	@JsonIgnore
	String getFieldName();
}
