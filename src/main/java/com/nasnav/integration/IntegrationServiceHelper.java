package com.nasnav.integration;

import com.nasnav.integration.events.data.CustomerData;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;

public interface IntegrationServiceHelper {

	void pushCustomerCreationEvent(CustomerData data, Long orgId);

	void pushOrderConfirmEvent(OrdersEntity order);

	void pushNewPaymentEvent(PaymentEntity payment);
}
