package com.nasnav.dto.rocketchat;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

// ignore any field named agents because it causes a problem
@JsonIgnoreProperties(ignoreUnknown = true, value = {"agents"})
@Data
public class RocketChatDTOWrapper<T extends RocketChatWrappedData> {

	@JsonIgnore
	private T data;

	@JsonAnySetter
	void setDetails(String key, T value) {
		if(value.getFieldName().equals(key)) {
			if (data != null) throw new IllegalStateException();
			data = value;
		}
	}

	@JsonAnyGetter
	Map<String, T> getDetails() {
		return Map.of(data.getFieldName(), data);
	}
}
