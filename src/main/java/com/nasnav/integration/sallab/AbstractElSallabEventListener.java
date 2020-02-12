package com.nasnav.integration.sallab;

import static com.nasnav.integration.enums.IntegrationParam.AUTH_SERVER_URL;
import static com.nasnav.integration.enums.IntegrationParam.SERVER_2_URL;
import static com.nasnav.integration.enums.IntegrationParam.SERVER_URL;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

import org.springframework.web.reactive.function.client.ClientResponse;

import com.nasnav.integration.IntegrationEventListener;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.sallab.webclient.SallabWebClient;

import reactor.core.publisher.Mono;

public abstract class AbstractElSallabEventListener<E extends Event<T,R>, T, R>  extends IntegrationEventListener<E, T, R> {

	private SallabWebClient client;
	
	
	
	public AbstractElSallabEventListener(IntegrationService integrationService) {
		super(integrationService);		
	}
	
	
	
	
	protected SallabWebClient getWebClient(Long orgId) {
		if(client == null) {
			String serverUrl = integrationService.getIntegrationParamValue(orgId, SERVER_URL.getValue());
			String server2Url = integrationService.getIntegrationParamValue(orgId, SERVER_2_URL.getValue());
			String authServerUrl = integrationService.getIntegrationParamValue(orgId, AUTH_SERVER_URL.getValue());
			client = new SallabWebClient(serverUrl, server2Url, authServerUrl);
		}
		
		return client;
	}
	
	
	

	
	
	protected Mono<ClientResponse> throwExceptionIfNotOk(ClientResponse response) {
		return just(response)
				.filter(res -> res.statusCode() == OK)
				.switchIfEmpty( error( getFailedResponseRuntimeException()));
	}




	protected RuntimeException getFailedResponseRuntimeException() {
		return new RuntimeException("Failed to get valid response from El-sallab API ");
	}




}
