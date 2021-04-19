package com.nasnav.test.integration.modules;

import com.nasnav.integration.IntegrationModule;
import com.nasnav.integration.IntegrationService;
import com.nasnav.test.integration.event.TestEvent;
import com.nasnav.test.integration.event.TestEvent2;
import com.nasnav.test.integration.event.TestEventWithHandlerInfo;
import com.nasnav.test.integration.event.handler.HandlingInfoSaver;
import com.nasnav.test.integration.event.handler.TestEvent2Handler;
import com.nasnav.test.integration.event.handler.TestEventHandler;

public class TestIntegrationModule extends IntegrationModule {

	public TestIntegrationModule(IntegrationService integrationService) {
		super(integrationService);
	}
	
	
	

	@Override
	protected void initEventListeners(IntegrationService integrationService) {
		this.addEventListener(TestEvent.class, new TestEventHandler(integrationService));
		this.addEventListener(TestEvent2.class, new TestEvent2Handler(integrationService));
		this.addEventListener(TestEventWithHandlerInfo.class, new HandlingInfoSaver(integrationService));
	}


}
