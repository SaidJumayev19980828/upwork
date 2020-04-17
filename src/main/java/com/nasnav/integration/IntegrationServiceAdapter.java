package com.nasnav.integration;

import com.nasnav.integration.events.data.CustomerData;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;

/**
 * Helper class for interfacing Integration service to entity listeners - which are not managed
 *  by spring context -.
 * */
public interface IntegrationServiceAdapter {

	void pushCustomerCreationEvent(CustomerData data, Long orgId);

	void pushOrderConfirmEvent(OrdersEntity order);

	void pushPaymentEvent(PaymentEntity payment);
}
