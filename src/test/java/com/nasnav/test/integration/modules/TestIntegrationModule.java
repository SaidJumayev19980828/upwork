package com.nasnav.test.integration.modules;

import com.nasnav.integration.IntegrationModule;
import com.nasnav.integration.IntegrationService;
import com.nasnav.test.integration.event.TestEvent;
import com.nasnav.test.integration.event.handler.TestEventHandler;

public class TestIntegrationModule extends IntegrationModule {

	public TestIntegrationModule(IntegrationService integrationService) {
		super(integrationService);
	}
	
	
	

	@Override
	protected void initIntegrationHandlers(IntegrationService integrationService) {
		this.putEventHandler(TestEvent.class, new TestEventHandler(integrationService));
	}


}
