package com.nasnav.integration.events;

import com.nasnav.integration.events.data.PaymentData;

public class PaymentCreateEvent extends Event<PaymentData, String>{

	public PaymentCreateEvent(Long organizationId, PaymentData eventData) {
		super(organizationId, eventData);
	}

}
