package com.nasnav.integration.enums;

import lombok.Getter;

public enum IntegrationParam {
	INTEGRATION_MODULE("INTEGRATION_MODULE")
	, MAX_REQUEST_RATE("MAX_REQUESTS_PER_SECOND")
	, DISABLED("DISABLED")
	, SERVER_URL ("SERVER_URL")
	, SERVER_2_URL ("SERVER_2_URL")
	, AUTH_SERVER_URL ("AUTH_SERVER_URL")
	, IMG_SERVER_URL ("IMG_SERVER_URL")
	, IMG_AUTH_SERVER_URL("IMG_AUTH_SERVER_URL");
	
	@Getter
	private String value;

	IntegrationParam(String value) {
		this.value = value;
	}
}
