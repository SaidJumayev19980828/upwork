package com.nasnav.integration.events;

import java.util.function.Consumer;

import com.nasnav.integration.events.data.PaymentData;

public class  PaymentCreateEvent extends Event<PaymentData, String>{

	public PaymentCreateEvent(Long organizationId, PaymentData eventData
			, Consumer<EventResult<PaymentData, String>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
