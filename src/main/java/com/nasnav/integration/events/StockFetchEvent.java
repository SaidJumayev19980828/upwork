package com.nasnav.integration.events;

import java.util.function.Consumer;

import com.nasnav.integration.events.data.StockEventParam;

public class StockFetchEvent extends Event<StockEventParam, Integer>{

	public StockFetchEvent(Long organizationId, StockEventParam eventData
			, Consumer<EventResult<StockEventParam, Integer>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}
	
	
	
	
	public StockFetchEvent(Long organizationId, StockEventParam eventData) {
		super(organizationId, eventData);
	}

}
