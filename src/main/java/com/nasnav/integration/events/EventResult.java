package com.nasnav.integration.events;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public abstract class EventResult<T,R> {
	private Event<T> event ;
	private R returnedData;
	private LocalDateTime timestamp;
	
	
	public EventResult(Event<T> event, R returnedData) {		
		this.event = event;
		this.returnedData = returnedData;
		this.timestamp = LocalDateTime.now();
	}
}
