package com.nasnav.integration.events;

import com.nasnav.integration.events.data.CustomerFetchParam;

public class CustomerFetchEvent extends Event<CustomerFetchParam>{

	public CustomerFetchEvent(Long organizationId, CustomerFetchParam eventData) {
		super(organizationId, eventData);
	}

}
