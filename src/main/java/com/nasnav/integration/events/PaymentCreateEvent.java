package com.nasnav.integration.events;

import com.nasnav.integration.events.data.PaymentData;

public class PaymentCreateEvent extends Event<PaymentData>{

	public PaymentCreateEvent(Long organizationId, PaymentData eventData) {
		super(organizationId, eventData);
	}

}
