package com.nasnav.integration.events;

import java.util.function.Consumer;

import com.nasnav.integration.events.data.ShopsFetchParam;
import com.nasnav.integration.model.IntegratedShop;

public class ShopsFetchEvent extends Event<ShopsFetchParam, IntegratedShop>{

	public ShopsFetchEvent(Long organizationId, ShopsFetchParam eventData
			, Consumer<EventResult<ShopsFetchParam, IntegratedShop>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
