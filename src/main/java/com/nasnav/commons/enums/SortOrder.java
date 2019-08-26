package com.nasnav.commons.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum SortOrder {
	ASC("asc"), DESC("desc");
	
	@Getter
	@JsonValue
    private final String value;
	
	@JsonCreator
	SortOrder(String value) {
        this.value = value;
    }
}