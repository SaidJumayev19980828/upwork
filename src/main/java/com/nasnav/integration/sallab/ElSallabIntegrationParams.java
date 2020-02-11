package com.nasnav.integration.sallab;

import lombok.Getter;

public enum ElSallabIntegrationParams {
	
	AUTH_GRANT_TYPE("COM.SALLAB.AUTH.GRANTTYPE"),
	CLIENT_ID("COM.SALLAB.AUTH.CLIENTID"), 
	CLIENT_SECRET("COM.SALLAB.AUTH.CLIENTSECRET"),
	USERNAME("COM.SALLAB.AUTH.USERNAME"), 
	PASSWORD("COM.SALLAB.AUTH.PASSWORD");
	
	@Getter
	private String value;

	ElSallabIntegrationParams(String value) {
		this.value = value;
	}
}
