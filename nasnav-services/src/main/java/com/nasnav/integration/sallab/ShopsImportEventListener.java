package com.nasnav.integration.sallab;

import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.ShopsImportEvent;
import com.nasnav.integration.events.data.ShopsFetchParam;
import com.nasnav.integration.model.ImportedShop;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.Arrays.asList;
import static reactor.core.publisher.Mono.just;

public class ShopsImportEventListener extends AbstractElSallabEventListener<ShopsImportEvent, ShopsFetchParam, List<ImportedShop>> {

	public static final int HARD_CODED_STOCK = 222;





	public ShopsImportEventListener(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	
	@Override
	protected Mono<List<ImportedShop>> handleEventAsync(EventInfo<ShopsFetchParam> event) {
		ImportedShop hardCodedShop = new ImportedShop();
		hardCodedShop.setId(String.valueOf(HARD_CODED_STOCK));
		hardCodedShop.setName("New Cairo");
		
		return just(asList(hardCodedShop));
	}

	
	
	
	
	@Override
	protected ShopsImportEvent handleError(ShopsImportEvent event, Throwable t) {
		// TODO Auto-generated method stub
		return null;
	}

}
