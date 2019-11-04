package com.nasnav.integration.events;

import com.nasnav.integration.events.data.ShopsFetchParam;
import com.nasnav.integration.model.IntegratedShop;

public class ShopsFetchEvent extends Event<ShopsFetchParam, IntegratedShop>{

	public ShopsFetchEvent(Long organizationId, ShopsFetchParam eventData) {
		super(organizationId, eventData);
	}

}
