package com.nasnav.integration.modules;

import com.nasnav.integration.IntegrationModule;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.OrderConfirmEvent;
import com.nasnav.integration.events.handlers.OrderConfirmEventHandler;





public class DoNothingModule extends IntegrationModule{

	
	public DoNothingModule(IntegrationService integrationService) {
		super(integrationService);
	}
	
	
	

	@Override
	protected void initIntegrationHandlers(IntegrationService integrationService) {
		OrderConfirmEventHandler orderEventHandler = new OrderConfirmEventHandler( integrationService );
		
		this.putEventHandler(OrderConfirmEvent.class, orderEventHandler);
	}

}
