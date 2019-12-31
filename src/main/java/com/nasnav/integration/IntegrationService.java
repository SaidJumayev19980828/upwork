package com.nasnav.integration;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;

import com.nasnav.dto.IntegrationParamDTO;
import com.nasnav.dto.IntegrationParamDeleteDTO;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventResult;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.integration.model.ImportedShop;
import com.nasnav.persistence.ShopsEntity;

import reactor.core.publisher.Mono;





public interface IntegrationService {
		
	void setIntegrationModule(Long orgId, String classFullName);
	IntegrationModule getIntegrationModule(Long orgId);
	void loadIntegrationModules() throws BusinessException;
	
	void addMappedValue(Long orgId, MappingType type, String localValue, String remoteValue) throws BusinessException;
	String getRemoteMappedValue(Long orgId, MappingType type, String localValue);
	String getLocalMappedValue(Long orgId, MappingType type, String externalValue);
	
	/**
	 * import the organization shops from an external system using the organization integration module.
	 * @return list of the new shops that were inserted into the system.
	 * */
	List<ShopsEntity> importOrganizationShops(Long orgId) throws Throwable;
	
	void mapToIntegratedShop(Long shopId, ImportedShop integratedShop);
	
	
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
	 * @return a Mono of the event result, which will run the onSuccess callback provided to the event, or can
	 *  be used to block the calling thread until the result is returned.
	 * @throws InvalidIntegrationEventException 
	 * */
	<E extends Event<T,R>, T, R> Mono<EventResult<T,R>> pushIntegrationEvent(E event, BiConsumer<E, Throwable> onError) throws InvalidIntegrationEventException;
	
	
	
	
	
	
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
	
	/**
	 * register an integration module to a certain organization.
	 * The integration module will be loaded by the classloader and MUST exist in the classpath prior the
	 * registeration. 
	 * @throws BusinessException 
	 * */
	void registerIntegrationModule(OrganizationIntegrationInfoDTO integrationInfo) throws BusinessException;
	
	
	/**
	 * disable integration module of an organization, it will remain loaded, but the organization events will be
	 * discarded.
	 * @throws BusinessException 
	 * */
	void disableIntegrationModule(Long organizationId) throws BusinessException;
	
	
	/**
	 * enables an already disabled integration module of an organization.
	 * @throws BusinessException 
	 * */
	void enableIntegrationModule(Long organizationId) throws BusinessException;
	
	
	/**
	 * Mainly used for tests.
	 * */
	void clearAllIntegrationModules();
	
	
	/**
	 * remove the integration module for the organization permanently.
	 * */
	void removeIntegrationModule(Long organizationId);
	
	
	/**
	 * add/update an integration parameter.
	 * If no parameter type exists with the given name, a new parameter type is created.
	 * */
	void addIntegrationParam(IntegrationParamDTO param) throws BusinessException;
	
	
	
	/**
	 * delete integration parameter for organization.
	 * If no parameter exists with the given oraganization id and parameter name, the method will just return.
	 * */
	void deleteIntegrationParam(IntegrationParamDeleteDTO param) throws BusinessException;
	
	
	
	List<OrganizationIntegrationInfoDTO> getAllIntegrationModules();
	
	
	void deleteMappingByLocalValue(Long orgId, MappingType product, String mappingLocalVal);
	
	void deleteMappingByRemoteValue(Long orgId, MappingType product, String mappingRemoteVal);
	
	
	/**
	 * @return the parameter value for the given organization and parameter type name.
	 * if the organization has no integration module or has no parameter with the given name, return null.
	 * */
	String getIntegrationParamValue(Long orgId, String paramName);
}
