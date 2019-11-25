package com.nasnav.integration.events;

import java.util.function.Consumer;

import com.nasnav.integration.events.data.StockParam;

public class StockFetchEvent extends Event<StockParam, String>{

	public StockFetchEvent(Long organizationId, StockParam eventData
			, Consumer<EventResult<StockParam, String>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
