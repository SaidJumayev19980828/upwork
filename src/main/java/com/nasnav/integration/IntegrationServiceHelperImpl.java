package com.nasnav.integration;

import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_CUSTOMER_MAPPING_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_INTEGRATION_EVENT_PROCESSING_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_INTEGRATION_EVENT_PUSH_FAILED;
import static java.lang.String.format;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.CustomerCreateEvent;
import com.nasnav.integration.events.EventResult;
import com.nasnav.integration.events.data.CustomerData;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;

import reactor.core.publisher.Mono;

@Service
public class IntegrationServiceHelperImpl implements IntegrationServiceHelper {
	
	private static Logger logger = LogManager.getLogger();
	
	
	
	@Autowired
	IntegrationService integrationService;
	
	

	@Override
	public Mono<EventResult<CustomerData, String>> pushCustomerCreationEvent(CustomerData customer, Long orgId) {
		CustomerCreateEvent event = new CustomerCreateEvent(orgId, customer, this::saveUserExternalId);
		Mono<EventResult<CustomerData, String>> result = Mono.empty();
		
		try {
			result = integrationService.pushIntegrationEvent(event, this::handleCustomerIntegrationError);
		} catch (InvalidIntegrationEventException e) {
			logger.error(
					format(ERR_INTEGRATION_EVENT_PUSH_FAILED, CustomerCreateEvent.class.getName(), customer.toString(), orgId)
					, e);
		}
		
		return result;
	}
	
	
	
	
	
	private void saveUserExternalId(EventResult<CustomerData, String> result) {
		CustomerData customer = result.getEventInfo().getEventData();
		Long orgId = result.getEventInfo().getOrganizationId();
		String remoteId = result.getReturnedData();
		
		try {
			integrationService.addMappedValue(orgId, MappingType.CUSTOMER, String.valueOf(customer.getId()), remoteId);
		} catch (BusinessException e) {
			logger.error( format(ERR_CUSTOMER_MAPPING_FAILED, customer.toString(), remoteId), e);
		}
	}
	
	
	
	
	
	private void handleCustomerIntegrationError(CustomerCreateEvent event, Throwable error) {
		Long orgId = event.getOrganizationId();
		logger.error( format(ERR_INTEGRATION_EVENT_PROCESSING_FAILED
							, CustomerCreateEvent.class.getName()
							, event.getEventInfo().getEventData()
							, orgId)
				, error);
		
		integrationService.runGeneralErrorFallback(event, error, null);		
	}
	
	
	
}
