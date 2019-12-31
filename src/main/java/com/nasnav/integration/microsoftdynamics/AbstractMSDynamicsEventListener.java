package com.nasnav.integration.microsoftdynamics;

import static com.nasnav.integration.enums.IntegrationParam.SERVER_URL;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.nasnav.integration.IntegrationEventListener;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.microsoftdynamics.webclient.FortuneWebClient;

public abstract class AbstractMSDynamicsEventListener<E extends Event<T,R>, T, R>  extends IntegrationEventListener<E, T, R> {

	private FortuneWebClient client;
	
	
	
	public AbstractMSDynamicsEventListener(IntegrationService integrationService) {
		super(integrationService);		
	}
	
	
	
	
	protected FortuneWebClient getWebClient(Long orgId) {
		if(client == null) {
			String serverUrl = integrationService.getIntegrationParamValue(orgId, SERVER_URL.getValue());
			client = new FortuneWebClient(serverUrl);
		}
		
		return client;
	}
	
	
	

	
	
	protected void throwExceptionIfNotOk(ClientResponse response) {
		if(response.statusCode() != HttpStatus.OK) {
			throw new RuntimeException("Failed to get valid response from Microsoft Dynamics API ");
		}
	}




}
