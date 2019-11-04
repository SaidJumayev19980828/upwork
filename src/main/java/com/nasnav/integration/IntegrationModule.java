package com.nasnav.integration;

import java.util.HashMap;
import java.util.Map;

import com.nasnav.integration.events.Event;

import lombok.Data;

@Data
public abstract class IntegrationModule {
	@SuppressWarnings("rawtypes")
	private Map< Class<Event>, IntegrationEventHandler > eventHandlers;
	protected IntegrationService integrationService;
	
	
	public IntegrationModule(IntegrationService integrationService) {
		eventHandlers = new HashMap<>();
		
		this.integrationService = integrationService;		
		initIntegrationHandlers(integrationService);
	}
	
	
	
	protected abstract void initIntegrationHandlers(IntegrationService integrationService2);




	@SuppressWarnings("unchecked")
	<E extends Event<T,R>, T,R> IntegrationEventHandler<E,T,R> getEventHandler(E event) {
		return (IntegrationEventHandler<E, T,R>)eventHandlers.get(event.getClass());		
	}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <E extends Event<T,R>, T,R> void putEventHandler(Class<E> eventClass , IntegrationEventHandler<E,T,R> handler){
		eventHandlers.put((Class<Event>)eventClass, handler);
	}
	
}
