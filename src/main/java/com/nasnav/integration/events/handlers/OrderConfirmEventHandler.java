package com.nasnav.integration.events.handlers;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.nasnav.integration.IntegrationEventHandler;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.OrderConfirmEvent;
import com.nasnav.integration.events.data.OrderData;

public class OrderConfirmEventHandler extends IntegrationEventHandler<OrderConfirmEvent, OrderData, String> {

	
	
	
	public OrderConfirmEventHandler(IntegrationService integrationService) {
		super(integrationService);		
	}
	
	
	
	

	@Override
	public OrderConfirmEvent handleError(OrderConfirmEvent event, Throwable t) {		
		return null;
	}
	
	
	
	

	@Override
	public void handleEventAsync(OrderConfirmEvent event, Consumer<OrderConfirmEvent> onComplete,
			BiConsumer<OrderConfirmEvent, Throwable> onError) {
		event.setEventResult( "PASSED!!" );
	}


}
