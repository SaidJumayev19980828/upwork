package com.nasnav.integration.events;

import java.util.function.Consumer;

import com.nasnav.integration.events.data.ProductImportEventParam;

public class ProductsImportEvent extends Event<ProductImportEventParam, IntegrationImportedProducts>{

	public ProductsImportEvent(Long organizationId, ProductImportEventParam eventData
			, Consumer<EventResult<ProductImportEventParam, IntegrationImportedProducts>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}
	
	
	
	public ProductsImportEvent(Long organizationId, ProductImportEventParam eventData) {
		super(organizationId, eventData);
	}

}
