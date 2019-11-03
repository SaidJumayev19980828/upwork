package com.nasnav.integration;

import javax.annotation.Nonnull;

import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventResult;


public abstract class IntegrationEventHandler< E extends Event<T>, Result extends  EventResult<T,R>, T, R> {
	
	protected IntegrationService integrationService; 
	
	public IntegrationEventHandler(@Nonnull IntegrationService integrationService) {
		this.integrationService = integrationService;
	}
	
	public abstract Result handleError(E event);	
	public abstract Result handleEvent(E event);	
}
