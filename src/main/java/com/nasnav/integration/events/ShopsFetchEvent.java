package com.nasnav.integration.events;

import java.util.List;
import java.util.function.Consumer;

import com.nasnav.integration.events.data.ShopsFetchParam;
import com.nasnav.integration.model.IntegratedShop;

public class ShopsFetchEvent extends Event<ShopsFetchParam, List<IntegratedShop>>{

	public ShopsFetchEvent(Long organizationId, ShopsFetchParam eventData
			, Consumer<EventResult<ShopsFetchParam, List<IntegratedShop>>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
