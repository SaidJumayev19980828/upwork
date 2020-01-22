package com.nasnav.integration.microsoftdynamics;

import com.nasnav.integration.IntegrationModule;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.CustomerCreateEvent;
import com.nasnav.integration.events.OrderConfirmEvent;
import com.nasnav.integration.events.PaymentCreateEvent;
import com.nasnav.integration.events.ProductsImportEvent;
import com.nasnav.integration.events.ShopsImportEvent;
import com.nasnav.integration.events.StockFetchEvent;

public class MsDynamicsIntegrationModule extends IntegrationModule {

	public MsDynamicsIntegrationModule(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	@Override
	protected void initEventListeners(IntegrationService integrationService) {
		addEventListener(CustomerCreateEvent.class, new CustomerCreateEventListener(integrationService));
		addEventListener(ShopsImportEvent.class, new ShopsImportEventListener(integrationService));
		addEventListener(ProductsImportEvent.class, new ProductImportEventListener(integrationService));
		addEventListener(StockFetchEvent.class, new StockFetchEventListener(integrationService));
		addEventListener(OrderConfirmEvent.class, new OrderConfirmEventListener(integrationService));
		addEventListener(PaymentCreateEvent.class, new PaymentCreateEventListener(integrationService));
	}

}
