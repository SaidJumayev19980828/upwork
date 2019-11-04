package com.nasnav.integration.events.handlers;

import java.time.LocalDateTime;

import com.nasnav.integration.IntegrationEventHandler;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.OrderConfirmEvent;
import com.nasnav.integration.events.data.OrderData;

public class OrderConfirmEventHandler extends IntegrationEventHandler<OrderConfirmEvent, OrderData, String> {

	
	
	
	public OrderConfirmEventHandler(IntegrationService integrationService) {
		super(integrationService);		
	}
	
	
	
	

	@Override
	public OrderConfirmEvent handleError(OrderConfirmEvent event) {		
		return null;
	}
	
	
	
	

	@Override
	public OrderConfirmEvent handleEvent(OrderConfirmEvent event) {
		event.setResultRecievedTime( LocalDateTime.now() );
		event.setEventResult( "PASSED!!" );
		return event;
	}

}
