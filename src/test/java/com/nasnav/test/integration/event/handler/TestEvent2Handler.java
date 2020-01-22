package com.nasnav.test.integration.event.handler;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import com.nasnav.integration.IntegrationEventListener;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.test.integration.event.TestEvent2;

import reactor.core.publisher.Mono;

public class TestEvent2Handler extends IntegrationEventListener<TestEvent2, Long, String> {
	
	public static final Long EXPECTED_DATA = 1234L;
	public static final String EXPECTED_RESULT = "Bye Event";
	
	public static Consumer<EventInfo<Long>> onHandle = e -> {};
	public static Consumer<TestEvent2> onError = e -> {};
	
	
	public TestEvent2Handler(IntegrationService integrationService) {
		super(integrationService);
	}

	



	@Override
	protected Mono<String> handleEventAsync(EventInfo<Long> event) {
		System.out.println("Hanlding Test Event! @ "+ LocalDateTime.now() );	
		onHandle.accept(event);	
		return Mono.just(EXPECTED_RESULT);
	}





	@Override
	protected TestEvent2 handleError(TestEvent2 event, Throwable t) {
		onError.accept(event);
		return event;
	}


}
