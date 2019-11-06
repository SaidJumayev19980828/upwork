package com.nasnav.test.integration.event.handler;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import com.nasnav.integration.IntegrationEventHandler;
import com.nasnav.integration.IntegrationService;
import com.nasnav.test.integration.event.TestEvent;

public class TestEventHandler extends IntegrationEventHandler<TestEvent, String, String> {
	
	public static final String EXPECTED_DATA = "Hi Event!";
	public static final String EXPECTED_RESULT = "Bye Event";
	
	public static Consumer<TestEvent> onHandle = e -> {};
	public static Consumer<TestEvent> onError = e -> {};
	
	
	public TestEventHandler(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	
	@Override
	public TestEvent handleEvent(TestEvent event) {
		
		onHandle.accept(event);
		
		event.setEventResult(EXPECTED_RESULT);
		event.setResultRecievedTime(LocalDateTime.now());		
		
		return event;
	}

	
	
	
	
	@Override
	public TestEvent handleError(TestEvent event) {	
		onError.accept(event);
		return event;
	}

}
