package com.nasnav.enumerations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum ImageFileTemplateType {
	EMPTY("empty"), PRODUCTS_WITH_NO_IMGS("product_with_no_imgs");
	
	@Getter
	@JsonValue
    private final String value;
	
	@JsonCreator
	ImageFileTemplateType(String value) {
        this.value = value;
    }
}
