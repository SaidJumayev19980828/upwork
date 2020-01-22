package com.nasnav.integration.enums;

import lombok.Getter;

public enum MappingType {
	PRODUCT_VARIANT("PRODUCT_VARIANT"), SHOP("SHOP"), ORDER("ORDER"), CUSTOMER("CUSTOMER"), PAYMENT("PAYMENT");
	
	
	@Getter
	private String value;

	MappingType(String value) {
		this.value = value;
	}
}
