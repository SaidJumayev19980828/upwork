package com.nasnav.integration.events;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public abstract class Event<D> {
	protected D eventData;
	protected LocalDateTime timestamp;
	protected Long organizationId;
	
	public Event(Long organizationId, D eventData) {
		this.eventData = eventData;
		this.timestamp = LocalDateTime.now();
	}
}
