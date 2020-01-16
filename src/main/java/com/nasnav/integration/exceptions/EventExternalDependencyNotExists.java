package com.nasnav.integration.exceptions;

import com.nasnav.integration.events.Event;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents an error that happened due to missing value that should be retrieved from external system, or 
 * should have a mapping in INTEGRATION_MAPPING table.
 * */
@Data
@EqualsAndHashCode(callSuper = true)
public  class EventExternalDependencyNotExists extends RuntimeException{
	private static final long serialVersionUID = 1879641644236L;

	@SuppressWarnings("rawtypes")
	private Event event;
	private String dependencyName;
	
	public <E extends Event<T,R>, T,R> EventExternalDependencyNotExists(E event, Class<? extends E> eventtype, String dependencyName){
		this.event = event;
		this.dependencyName = dependencyName;
	}
	
	
	
	
	
	public <E extends Event<T,R>, T,R> E getEvent(Class<? extends E> eventType){
		return eventType.cast(event);
	}
}
