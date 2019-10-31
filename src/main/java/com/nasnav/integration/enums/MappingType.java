package com.nasnav.integration.enums;

import lombok.Getter;

public enum MappingType {
	PRODUCT(0), SHOP(1), ORDER(3), CUSTOMER(4), PAYMENT(5);
	
	
	@Getter
	private Integer value;

	MappingType(Integer value) {
		this.value = value;
	}
}
