package com.nasnav.integration.events;

import java.util.function.Consumer;

import com.nasnav.integration.events.data.ProductFetchParam;

public class ProductMassFetchEvent extends Event<ProductFetchParam, String>{

	public ProductMassFetchEvent(Long organizationId, ProductFetchParam eventData
			, Consumer<EventResult<ProductFetchParam, String>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
