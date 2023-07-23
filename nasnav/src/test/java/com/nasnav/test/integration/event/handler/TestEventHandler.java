package com.nasnav.test.integration.event.handler;

import com.nasnav.integration.IntegrationEventListener;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.test.integration.event.TestEvent;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Slf4j
public class TestEventHandler extends IntegrationEventListener<TestEvent, String, String> {
	
	public static final String EXPECTED_DATA = "Hi Event!";
	public static final String EXPECTED_RESULT = "Bye Event";
	
	public static Consumer<EventInfo<String>> onHandle = e -> {};
	public static Consumer<TestEvent> onError = e -> {};
	
	
	public TestEventHandler(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	
	@Override
	protected Mono<String> handleEventAsync(EventInfo<String> event) {
		log.debug("Hanlding Test Event! @ {}", LocalDateTime.now() );	
		log.debug("Hanlding Test Event with data : {}", event.toString() );
		onHandle.accept(event);
		return Mono.just(EXPECTED_RESULT);
	}

	
	
	
	
	@Override
	protected TestEvent handleError(TestEvent event, Throwable t) {	
		onError.accept(event);
		return event;
	}


}
