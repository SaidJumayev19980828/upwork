package com.nasnav.integration.events.results;

import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventResult;
import com.nasnav.integration.events.data.OrderData;

public class OrderConfirmEventResult extends EventResult<OrderData, String> {

	public OrderConfirmEventResult(Event<OrderData> event, String returnedData) {
		super(event, returnedData);		
	}

}
