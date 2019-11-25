package com.nasnav.integration.events.handlers;

import com.nasnav.integration.IntegrationEventListener;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.OrderConfirmEvent;
import com.nasnav.integration.events.data.OrderData;

import reactor.core.publisher.Mono;

public class OrderConfirmEventHandler extends IntegrationEventListener<OrderConfirmEvent, OrderData, String> {

	
	
	
	public OrderConfirmEventHandler(IntegrationService integrationService) {
		super(integrationService);		
	}
	
	
	
	

	@Override
	public OrderConfirmEvent handleError(OrderConfirmEvent event, Throwable t) {		
		return null;
	}
	
	
	
	

	@Override
	public Mono<String> handleEventAsync(EventInfo<OrderData> event) {
		return Mono.just("PASSED!!");
	}


}
