package com.nasnav.test.integration.event.handler;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import com.nasnav.integration.IntegrationEventListener;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.test.integration.event.HandlingInfo;
import com.nasnav.test.integration.event.TestEventWithHandlerInfo;

import reactor.core.publisher.Mono;

public class HandlingInfoSaver extends IntegrationEventListener<TestEventWithHandlerInfo, Integer, HandlingInfo> {

	public static long HANDLING_TIME = 1000;
	public static Consumer<TestEventWithHandlerInfo> onHandle = e -> {};
	public static Consumer<TestEventWithHandlerInfo> onError = e -> {};
	
	
	public HandlingInfoSaver(IntegrationService integrationService) {
		super(integrationService);
	}
	
	
	
	

	@Override
	protected Mono<HandlingInfo> handleEventAsync(EventInfo<Integer> event) {
		HandlingInfo info = new HandlingInfo();
		info.setHandlingStartTime( LocalDateTime.now() );
		info.setThread( Thread.currentThread() );
		
		try {
			Thread.sleep(HANDLING_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return Mono.just(info);
	}
	
	
	

	@Override
	protected TestEventWithHandlerInfo handleError(TestEventWithHandlerInfo event, Throwable t) {
		onError.accept(event);
		return event;
	}

}
