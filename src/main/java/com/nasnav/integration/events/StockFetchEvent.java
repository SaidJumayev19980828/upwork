package com.nasnav.integration.events;

import com.nasnav.integration.events.data.StockParam;

public class StockFetchEvent extends Event<StockParam, String>{

	public StockFetchEvent(Long organizationId, StockParam eventData) {
		super(organizationId, eventData);
	}

}
