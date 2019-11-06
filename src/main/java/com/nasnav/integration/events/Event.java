package com.nasnav.integration.events;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public abstract class Event<D, R> {
	protected D eventData;
	protected R eventResult;
	protected LocalDateTime creationTime;
	protected LocalDateTime resultRecievedTime;
	protected Long organizationId;
	
	
	
	public Event(Long organizationId, D eventData) {
		this.organizationId = organizationId;
		this.eventData = eventData;
		this.creationTime = LocalDateTime.now();
	}
}
