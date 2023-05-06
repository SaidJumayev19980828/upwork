package com.nasnav.dto.rocketchat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
interface RocketChatWrappedData {
	String getFieldName();
}
