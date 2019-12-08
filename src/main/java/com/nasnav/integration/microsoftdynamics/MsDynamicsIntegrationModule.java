package com.nasnav.integration.microsoftdynamics;

import com.nasnav.integration.IntegrationModule;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.CustomerCreateEvent;

public class MsDynamicsIntegrationModule extends IntegrationModule {

	public MsDynamicsIntegrationModule(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	@Override
	protected void initEventListeners(IntegrationService integrationService) {
		addEventListener(CustomerCreateEvent.class, new CustomerCreateEventListener(integrationService));
	}

}
