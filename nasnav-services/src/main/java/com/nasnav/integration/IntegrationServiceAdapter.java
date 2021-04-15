package com.nasnav.integration;

import com.nasnav.integration.events.data.CustomerData;
import com.nasnav.persistence.MetaOrderEntity;

/**
 * Helper class for interfacing Integration service to entity listeners - which are not managed
 *  by spring context -.
 * */
public interface IntegrationServiceAdapter {

	void pushCustomerCreationEvent(CustomerData data, Long orgId);

	void pushMetaOrderFinalizeEvent(MetaOrderEntity order);
}
