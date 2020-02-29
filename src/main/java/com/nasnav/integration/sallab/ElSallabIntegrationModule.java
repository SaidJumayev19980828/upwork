package com.nasnav.integration.sallab;

import com.nasnav.integration.IntegrationModule;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.ImagesImportEvent;
import com.nasnav.integration.events.ProductsImportEvent;
import com.nasnav.integration.events.ShopsImportEvent;

public class ElSallabIntegrationModule extends IntegrationModule{

	public ElSallabIntegrationModule(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	
	@Override
	protected void initEventListeners(IntegrationService integrationService) {
		addEventListener(ShopsImportEvent.class, new ShopsImportEventListener(integrationService));
		addEventListener(ProductsImportEvent.class, new ProductImportEventListener(integrationService));
		addEventListener(ImagesImportEvent.class , new ImageImportEventListener(integrationService));
	}

}
