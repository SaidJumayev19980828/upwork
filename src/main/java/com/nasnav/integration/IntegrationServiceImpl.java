package com.nasnav.integration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventResult;
import com.nasnav.integration.model.IntegratedShop;


@Service
public class IntegrationServiceImpl implements IntegrationService {

	private Map<Long, ? extends IntegrationModule> modules;
	
	
	
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
		// TODO Auto-generated method stub

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
	public <T, R> void pushIntegrationEvent(Event<T> event, Consumer<EventResult<T, R>> callback) {
		// TODO Auto-generated method stub

	}

}
