package com.nasnav.integration.events;

import com.nasnav.integration.events.data.ProductFetchParam;

public class ProductMassFetchEvent extends Event<ProductFetchParam, String>{

	public ProductMassFetchEvent(Long organizationId, ProductFetchParam eventData) {
		super(organizationId, eventData);
	}

}
