package com.nasnav.integration.events;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventResult<T,R> {
	protected EventInfo<T> eventInfo;
	protected R returnedData;
	protected LocalDateTime resultRecievedTime;
	
	
	public EventResult(EventInfo<T> eventInfo, R returnedData) {
		this.resultRecievedTime = LocalDateTime.now();
		this.eventInfo = eventInfo;
		this.returnedData = returnedData;
	}
}
