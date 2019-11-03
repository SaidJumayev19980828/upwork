package com.nasnav.integration;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventResult;
import com.nasnav.integration.model.IntegratedShop;





public interface IntegrationService {
		
	void setIntegrationModule(Long orgId, String classFullName);
	IntegrationModule getIntegrationModule(Long orgId);
	void loadIntegrationModules();
	
	void addMappedValue(Long orgId, MappingType type, String localValue, String remoteValue);
	String getExternalMappedValue(Long orgId, MappingType type, String localValue);
	String getLocalMappedValue(Long orgId, MappingType type, String externalValue);
	
	/**
	 * Get the organization shops from an external system using the organization integration module.
	 * */
	List<IntegratedShop> fetchOrganizationShops(Long orgId);
	void mapToIntegratedShop(Long shopId, IntegratedShop integratedShop);
	
	
	/**
	 * Import the organization products from an external system, the run the callback when finished.
	 * */
	void importOrganizationProducts(Long orgId, Runnable onComplete, Runnable onError);
	
	
	/**
	 * return the stock of a product in the external system.
	 * */
	BigDecimal getExternalStock(Long localStockId, Runnable onComplete, Runnable onError);
	
	
	/**
	 * push an event to the Integration service, which queues and run the proper event handler for the event
	 * based on the organization and the event type.
	 * */
	<T,R> void pushIntegrationEvent(Event<T> event, Consumer<EventResult<T,R>> callback);
}
