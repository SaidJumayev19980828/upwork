package com.nasnav.integration.events;

import com.nasnav.integration.events.data.OrderData;

public class OrderConfirmEvent extends Event<OrderData, String>{

	public OrderConfirmEvent(Long organizationId, OrderData eventData) {
		super(organizationId, eventData);
	}

}
