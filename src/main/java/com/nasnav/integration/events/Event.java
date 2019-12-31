package com.nasnav.integration.events;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import lombok.Data;
import lombok.Getter;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@Data
public abstract class Event<T, R> {
	protected EventInfo<T> eventInfo;
	@Getter
	protected Mono<EventResult<T,R>> eventResult;
	protected FluxSink<EventResult<T,R>> eventResultFluxSink;
	
	
	public Event(Long organizationId, T eventData, Consumer<EventResult<T,R>> onSuccess) {
		this.eventInfo = new EventInfo<>(organizationId, eventData);
		
		EmitterProcessor<EventResult<T,R>> emitterProcessor = EmitterProcessor.create();
		eventResult = emitterProcessor
							.publish()
							.autoConnect()
							.next();			
		eventResult.subscribe(onSuccess);
		eventResultFluxSink = emitterProcessor.sink();
	}
	
	
	
	public Event(Long organizationId, T eventData) {
		this(organizationId, eventData, res -> {});
	}
	
	
	
	/**
	 * sinks the result data into the EventResult Mono, which notifies any subscriber to this Mono.
	 * */
	public void broadcastResultData(R resultData) {
		EventResult<T,R> evResult = new EventResult<>(eventInfo, resultData);
		eventResultFluxSink.next(evResult);
	}
	
	
	
	
	public Long getOrganizationId() {
		return eventInfo.getOrganizationId();
	}
	
	
	
	public LocalDateTime getCreationTime() {
		return eventInfo.getCreationTime();
	}
}
