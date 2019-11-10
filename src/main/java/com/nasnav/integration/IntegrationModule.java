package com.nasnav.integration;

import java.util.HashMap;
import java.util.Map;

import com.nasnav.integration.events.Event;

import lombok.Data;


@Data
public abstract class IntegrationModule {
	@SuppressWarnings("rawtypes")
	private Map< Class<? extends Event>, IntegrationEventHandler > eventHandlers;
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
	
	
	
	
	
	protected <E extends Event<T,R>, T,R> void putEventHandler(Class<E> eventClass , IntegrationEventHandler<E,T,R> handler){
		eventHandlers.put(eventClass, handler);
	}


	
	

	public <E extends Event<T,R>, T,R>  void pushEvent(EventHandling<E,T,R> handling) {
		IntegrationEventHandler<E,T,R> handler = this.getEventHandler(handling.event);
		if(handler == null) {
			return; 	//ignore events with no handlers for this organization
		}
		
		handler.pushEvent(handling.getEvent(), handling.getOnComplete(), handling.getOnError());
	};
	
}
