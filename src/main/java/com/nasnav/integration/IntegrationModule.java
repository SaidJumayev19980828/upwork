package com.nasnav.integration;

import java.util.HashMap;
import java.util.Map;

import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventResult;

import lombok.Data;

@Data
public abstract class IntegrationModule {
	protected Map< Class<? extends Event >, IntegrationEventHandler > eventHandlers;
	protected IntegrationService integrationService;
	
	
	public IntegrationModule(IntegrationService integrationService) {
		eventHandlers = new HashMap<>();
		
		this.integrationService = integrationService;		
		initIntegrationHandlers(integrationService);
	}
	
	
	
	protected abstract void initIntegrationHandlers(IntegrationService integrationService2);




	<E extends Event<T>, T,R> IntegrationEventHandler<E, ? extends EventResult<T,R>, T,R> getEventHandler(E event) {
		return eventHandlers.get(event.getClass());		
	}
	
	
	
	
}
