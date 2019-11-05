package com.nasnav.integration.enums;

import lombok.Getter;

public enum IntegrationParam {
	INTEGRATION_MODULE("INTEGRATION_MODULE");
	
	@Getter
	private String value;

	IntegrationParam(String value) {
		this.value = value;
	}
}
