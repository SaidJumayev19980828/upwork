package com.nasnav.integration.sallab;

import static com.nasnav.integration.enums.IntegrationParam.AUTH_SERVER_URL;
import static com.nasnav.integration.enums.IntegrationParam.SERVER_2_URL;
import static com.nasnav.integration.enums.IntegrationParam.SERVER_URL;
import static java.lang.String.format;
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
				.flatMap(this::checkResponse);
	}
	
	
	
	
	protected Mono<ClientResponse> checkResponse(ClientResponse response){
		if(response.statusCode() == OK) {
			return Mono.just(response);
		}else {
			return error( getFailedResponseRuntimeException(response));
		}
	}
	



	protected RuntimeException getFailedResponseRuntimeException(ClientResponse response) {
		return new RuntimeException(
				format("Failed to get valid response from El-sallab API! failed response [%s] ", getResponseAsStr(response)));
	}




	private String getResponseAsStr(ClientResponse response) {
		response.bodyToMono(String.class).subscribe(b -> logger.info(format(" >>> El Sallab failed response body [%s]" , b)));
		return format("{status : %s}", response.statusCode());
	}




}
