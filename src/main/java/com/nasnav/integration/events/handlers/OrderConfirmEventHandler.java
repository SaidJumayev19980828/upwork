package com.nasnav.integration.events.handlers;

import com.nasnav.integration.IntegrationEventHandler;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.OrderConfirmEvent;
import com.nasnav.integration.events.data.OrderData;
import com.nasnav.integration.events.results.OrderConfirmEventResult;

public class OrderConfirmEventHandler extends IntegrationEventHandler<OrderConfirmEvent, OrderConfirmEventResult, OrderData, String> {

	
	
	
	public OrderConfirmEventHandler(IntegrationService integrationService) {
		super(integrationService);		
	}
	
	
	
	

	@Override
	public OrderConfirmEventResult handleError(OrderConfirmEvent event) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	

	@Override
	public OrderConfirmEventResult handleEvent(OrderConfirmEvent event) {
		return new OrderConfirmEventResult(event, "AXOB1234");
	}

}
