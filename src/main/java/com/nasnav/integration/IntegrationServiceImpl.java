package com.nasnav.integration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;

import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.model.IntegratedShop;


@Service
public class IntegrationServiceImpl implements IntegrationService {
	Logger logger = Logger.getLogger(getClass());
	private Map<Long, IntegrationModule> modules;
	
	
	
	@PostConstruct
	public void init() {
		modules = new HashMap<>();
		loadIntegrationModules();
	}
	
	
	
	
	@Override
	public void setIntegrationModule(Long orgId, String classFullName) {
		// TODO Auto-generated method stub

	}
	
	
	
	

	@Override
	public IntegrationModule getIntegrationModule(Long orgId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	

	@Override
	public void loadIntegrationModules() {
		
		try {
			Class<IntegrationModule> moduleClass = (Class<IntegrationModule>) this.getClass().getClassLoader().loadClass("com.nasnav.integration.events.handlers.DoNothingModule");
			modules.put(99001L, moduleClass.newInstance() );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	
	

	@Override
	public void addMappedValue(Long orgId, MappingType type, String localValue, String remoteValue) {
		// TODO Auto-generated method stub

	}
	
	
	
	

	@Override
	public String getExternalMappedValue(Long orgId, MappingType type, String localValue) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	

	@Override
	public String getLocalMappedValue(Long orgId, MappingType type, String externalValue) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	

	@Override
	public List<IntegratedShop> fetchOrganizationShops(Long orgId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	

	@Override
	public void mapToIntegratedShop(Long shopId, IntegratedShop integratedShop) {
		// TODO Auto-generated method stub

	}
	
	
	
	
	

	@Override
	public void importOrganizationProducts(Long orgId, Runnable onComplete, Runnable onError) {
		// TODO Auto-generated method stub

	}
	
	
	
	

	@Override
	public BigDecimal getExternalStock(Long localStockId, Runnable onComplete, Runnable onError) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	

	@Override
	public <T, R> void pushIntegrationEvent(Event<T,R> event, Consumer<Event<T, R>> onComplete, BiConsumer<Event<T, R>, Throwable> onError) {
		try {
			validateEvent(event);
			
			IntegrationModule mod = modules.get(event.getOrganizationId());
			Event<T, R> res =  mod.getEventHandler(event).handleEvent(event);
			
			onComplete.accept(res);
		}catch(Exception e) {
			logger.error(e);
			onError.accept(event,e);
		}		
	}



	

	private <T,R> void validateEvent(Event<T,R> event) {
		if(event == null || event.getOrganizationId() == null) {
			
		}
	}

}
