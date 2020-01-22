package com.nasnav.integration.events;

import java.util.List;
import java.util.function.Consumer;

import com.nasnav.integration.events.data.ShopsFetchParam;
import com.nasnav.integration.model.ImportedShop;

public class ShopsImportEvent extends Event<ShopsFetchParam, List<ImportedShop>>{

	public ShopsImportEvent(Long organizationId, ShopsFetchParam eventData
			, Consumer<EventResult<ShopsFetchParam, List<ImportedShop>>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}
	
	
	
	
	
	public ShopsImportEvent(Long organizationId, ShopsFetchParam eventData) {
		super(organizationId, eventData);
	}

}
