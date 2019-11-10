package com.nasnav.integration;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.model.IntegratedShop;





public interface IntegrationService {
		
	void setIntegrationModule(Long orgId, String classFullName);
	IntegrationModule getIntegrationModule(Long orgId);
	void loadIntegrationModules() throws BusinessException;
	
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
	<E extends Event<T,R>, T, R> void pushIntegrationEvent(E event, Consumer<E> onComplete, BiConsumer<E, Throwable> onError);
	
	
	
	/**
	 * called when the event handler fallback logic fails.
	 * i.e: fallback of the fallback
	 * */
	<E extends Event<T,R>, T, R> void runGeneralErrorFallback(E event, Throwable handlingException, Throwable fallbackException);
	
	
	
	/**
	 * save an event handling failure to the database.
	 * @param event the integration event
	 * @param handlingException the exception that was thrown when the event was being handled.
	 * @param fallbackException the exception that was thrown when the handling fallback logic was running.
	 * */
	public <E extends Event<T, R> ,T,R> void saveEventFailureToDB(E event, Throwable handlingException,
			Throwable fallbackException);
}
