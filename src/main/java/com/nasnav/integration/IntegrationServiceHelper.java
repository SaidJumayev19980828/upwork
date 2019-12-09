package com.nasnav.integration;

import com.nasnav.integration.events.EventResult;
import com.nasnav.integration.events.data.CustomerData;

import reactor.core.publisher.Mono;

public interface IntegrationServiceHelper {

	Mono<EventResult<CustomerData, String>> pushCustomerCreationEvent(CustomerData data, Long orgId);
}
