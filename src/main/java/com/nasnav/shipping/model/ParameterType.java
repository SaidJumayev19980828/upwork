package com.nasnav.shipping.model;

import org.json.JSONArray;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter

public enum ParameterType {
	STRING("String", String.class)
	, NUMBER("Number", Number.class)
	, STRING_ARRAY("StringArray", JSONArray.class)
	, LONG_ARRAY("LongArray", JSONArray.class);
	
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
