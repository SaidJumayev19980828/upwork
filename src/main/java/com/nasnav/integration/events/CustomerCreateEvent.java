package com.nasnav.integration.events;

import java.util.function.Consumer;

import com.nasnav.integration.events.data.CustomerData;

public class CustomerCreateEvent extends Event<CustomerData, String>{

	public CustomerCreateEvent(Long organizationId, CustomerData eventData
			, Consumer<EventResult<CustomerData, String>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}
}
