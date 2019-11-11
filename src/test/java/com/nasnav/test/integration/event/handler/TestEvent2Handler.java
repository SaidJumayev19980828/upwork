package com.nasnav.test.integration.event.handler;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.nasnav.integration.IntegrationEventHandler;
import com.nasnav.integration.IntegrationService;
import com.nasnav.test.integration.event.TestEvent;
import com.nasnav.test.integration.event.TestEvent2;

public class TestEvent2Handler extends IntegrationEventHandler<TestEvent2, Long, String> {
	
	public static final Long EXPECTED_DATA = 1234L;
	public static final String EXPECTED_RESULT = "Bye Event";
	
	public static Consumer<TestEvent2> onHandle = e -> {};
	public static Consumer<TestEvent2> onError = e -> {};
	
	
	public TestEvent2Handler(IntegrationService integrationService) {
		super(integrationService);
	}

	



	@Override
	protected void handleEventAsync(TestEvent2 event, Consumer<TestEvent2> onComplete,
			BiConsumer<TestEvent2, Throwable> onError) {
		System.out.println("Hanlding Test Event! @ "+ LocalDateTime.now() );	
		event.setEventResult(EXPECTED_RESULT);
		onHandle.accept(event);	
		onComplete.accept(event);
	}





	@Override
	protected TestEvent2 handleError(TestEvent2 event, Throwable t) {
		onError.accept(event);
		return event;
	}


}
