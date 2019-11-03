package com.nasnav.integration.events;

import com.nasnav.integration.events.data.ProductFetchParam;

public class ProductMassFetchEvent extends Event<ProductFetchParam>{

	public ProductMassFetchEvent(Long organizationId, ProductFetchParam eventData) {
		super(organizationId, eventData);
	}

}
