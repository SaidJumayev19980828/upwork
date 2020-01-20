package com.nasnav.integration.microsoftdynamics;

import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;

import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.ShopsImportEvent;
import com.nasnav.integration.events.data.ShopsFetchParam;
import com.nasnav.integration.microsoftdynamics.webclient.dto.GetStoresReponse;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Store;
import com.nasnav.integration.model.ImportedShop;

import reactor.core.publisher.Mono;

public class ShopsImportEventListener extends AbstractMSDynamicsEventListener<ShopsImportEvent, ShopsFetchParam, List<ImportedShop>> {

	public ShopsImportEventListener(IntegrationService integrationService) {
		super(integrationService);		
	}
		
	
	

	@Override
	protected Mono<List<ImportedShop>> handleEventAsync(EventInfo<ShopsFetchParam> event) {
		Long orgId = event.getOrganizationId();
		return getWebClient(orgId)
				.getStores()
				.filter( res -> res.statusCode() == HttpStatus.OK)
				.flatMap(this::throwExceptionIfNotOk)				
				.flatMap(res -> res.bodyToMono(GetStoresReponse.class))
				.map(this::getStoreList)
				.map(this::toImportedShopsList);
	}
	
	
	

	
	
	@Override
	protected ShopsImportEvent handleError(ShopsImportEvent event, Throwable t) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	private List<Store> getStoreList(GetStoresReponse response){
		return ofNullable(response)
				.map(GetStoresReponse::getResults)
				.map(List::stream)
				.flatMap(Stream::findFirst)
				.map(stores -> stores.getStores())
				.orElse(Collections.emptyList());			
	}
	
	
	
	
	private List<ImportedShop> toImportedShopsList(List<Store> stores){
		return ofNullable(stores)
				.orElse(Collections.emptyList())
				.stream()
				.map(this::toImportedShop)
				.collect(Collectors.toList());
	}
	
	
	
	
	private ImportedShop toImportedShop(Store store) {
		return new ImportedShop(store.getId(), store.getName());
	}

}
