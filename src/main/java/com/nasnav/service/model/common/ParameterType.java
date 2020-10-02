package com.nasnav.service.model.common;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter

public enum ParameterType {
	STRING("String", String.class)
	, NUMBER("Number", Number.class)
	, STRING_ARRAY("StringArray", JSONArray.class)
	, LONG_ARRAY("LongArray", JSONArray.class)
	, JSON("Json", JSONObject.class);
	
	@JsonValue
    private final String value;
	@JsonValue
	private final Class<?> javaType;
	
	@JsonCreator
	ParameterType(String value, Class<?> javaType) {
        this.value = value;
        this.javaType = javaType;
    }
}
