package com.nasnav.integration.events;

import com.nasnav.integration.events.data.CustomerData;

public class CustomerCreateEvent extends Event<CustomerData>{

	public CustomerCreateEvent(Long organizationId, CustomerData eventData) {
		super(organizationId, eventData);
	}
}
