package com.nasnav.integration.events;

import java.util.function.Consumer;

import com.nasnav.integration.events.data.OrderData;

public class OrderConfirmEvent extends Event<OrderData, String>{

	public OrderConfirmEvent(Long organizationId, OrderData eventData
			, Consumer<EventResult<OrderData, String>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
