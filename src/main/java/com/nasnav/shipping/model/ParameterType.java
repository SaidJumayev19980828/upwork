package com.nasnav.shipping.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum ParameterType {
	STRING("String"), LONG("Long"), STRING_ARRAY("StringArray");
	
	@Getter
	@JsonValue
    private final String value;
	
	@JsonCreator
	ParameterType(String value) {
        this.value = value;
    }
}
