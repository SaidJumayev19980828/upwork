package com.nasnav.test.integration.event.handler;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.nasnav.integration.IntegrationEventListener;
import com.nasnav.integration.IntegrationService;
import com.nasnav.test.integration.event.TestEvent;

public class TestEventHandler extends IntegrationEventListener<TestEvent, String, String> {
	
	public static final String EXPECTED_DATA = "Hi Event!";
	public static final String EXPECTED_RESULT = "Bye Event";
	
	public static Consumer<TestEvent> onHandle = e -> {};
	public static Consumer<TestEvent> onError = e -> {};
	
	
	public TestEventHandler(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	
	@Override
	protected void handleEventAsync(TestEvent event, Consumer<TestEvent> onComplete, BiConsumer<TestEvent, Throwable> onError) {
		System.out.println("Hanlding Test Event! @ "+ LocalDateTime.now() );	
		event.setEventResult(EXPECTED_RESULT);
		onHandle.accept(event);	
		onComplete.accept(event);
	}

	
	
	
	
	@Override
	protected TestEvent handleError(TestEvent event, Throwable t) {	
		onError.accept(event);
		return event;
	}


}
