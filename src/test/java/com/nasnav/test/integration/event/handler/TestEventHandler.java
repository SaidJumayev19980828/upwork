package com.nasnav.test.integration.event.handler;

import com.nasnav.integration.IntegrationEventHandler;
import com.nasnav.integration.IntegrationService;
import com.nasnav.test.integration.event.TestEvent;

public class TestEventHandler extends IntegrationEventHandler<TestEvent, String, String> {

	public TestEventHandler(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	
	@Override
	public TestEvent handleEvent(TestEvent event) {
		return event;
	}

	
	
	
	
	@Override
	public TestEvent handleError(TestEvent event) {
		return event;
	}

}
