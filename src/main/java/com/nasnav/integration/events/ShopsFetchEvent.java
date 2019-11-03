package com.nasnav.integration.events;

import com.nasnav.integration.events.data.ShopsFetchParam;

public class ShopsFetchEvent extends Event<ShopsFetchParam>{

	public ShopsFetchEvent(Long organizationId, ShopsFetchParam eventData) {
		super(organizationId, eventData);
	}

}
