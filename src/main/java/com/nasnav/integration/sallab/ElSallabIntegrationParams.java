package com.nasnav.integration.sallab;

import lombok.Getter;

public enum ElSallabIntegrationParams {
	
	AUTH_GRANT_TYPE("com.sallab.auth.grantType"),
	CLIENT_ID("com.sallab.auth.clientId"), 
	CLIENT_SECRET("com.sallab.auth.clientSecret"),
	USERNAME("com.sallab.auth.username"), 
	PASSWORD("com.sallab.auth.password");
	
	@Getter
	private String value;

	ElSallabIntegrationParams(String value) {
		this.value = value;
	}
}
