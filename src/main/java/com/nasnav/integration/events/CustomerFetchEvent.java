package com.nasnav.integration.events;

import java.util.function.Consumer;

import com.nasnav.integration.events.data.CustomerFetchParam;

public class CustomerFetchEvent extends Event<CustomerFetchParam, String>{

	public CustomerFetchEvent(Long organizationId, CustomerFetchParam eventData
				, Consumer<EventResult<CustomerFetchParam, String>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
