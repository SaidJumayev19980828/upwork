package com.nasnav.integration;

import java.util.HashMap;
import java.util.Map;

import com.nasnav.integration.events.Event;

import lombok.Data;


@Data
public abstract class IntegrationModule {
	@SuppressWarnings("rawtypes")
	private Map< Class<? extends Event>, IntegrationEventListener > eventListeners;
	protected IntegrationService integrationService;
	
	
	public IntegrationModule(IntegrationService integrationService) {
		eventListeners = new HashMap<>();
		
		this.integrationService = integrationService;		
		initIntegrationHandlers(integrationService);
	}
	
	
	
	protected abstract void initIntegrationHandlers(IntegrationService integrationService2);




	@SuppressWarnings("unchecked")
	<E extends Event<T,R>, T,R> IntegrationEventListener<E,T,R> getEventListiner(E event) {
		return (IntegrationEventListener<E, T,R>)eventListeners.get(event.getClass());		
	}
	
	
	
	
	
	protected <E extends Event<T,R>, T,R> void putEventHandler(Class<E> eventClass , IntegrationEventListener<E,T,R> handler){
		eventListeners.put(eventClass, handler);
	}


	
	

	public <E extends Event<T,R>, T,R>  void pushEvent(EventHandling<E,T,R> handling) {
		IntegrationEventListener<E,T,R> listerner = this.getEventListiner(handling.getEvent());
		if(listerner == null) {
			return; 	//ignore events with no listners
		}
		
		listerner.pushEvent(handling.getEvent(), handling.getOnError());
	};
	
}
