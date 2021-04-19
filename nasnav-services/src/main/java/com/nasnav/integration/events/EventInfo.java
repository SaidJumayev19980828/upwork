package com.nasnav.integration.events;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventInfo<T> {
	protected T eventData;
	protected LocalDateTime creationTime;
	protected Long organizationId;
	
	
	
	public EventInfo(Long organizationId, T eventData) {
		this.organizationId = organizationId;
		this.eventData = eventData;
		this.creationTime = LocalDateTime.now();
	}
}
