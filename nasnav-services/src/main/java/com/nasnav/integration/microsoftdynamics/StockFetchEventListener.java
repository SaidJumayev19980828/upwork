package com.nasnav.integration.microsoftdynamics;

import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.StockFetchEvent;
import com.nasnav.integration.events.data.StockEventParam;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Product;
import com.nasnav.integration.microsoftdynamics.webclient.dto.ProductsResponse;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Stocks;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;

import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class StockFetchEventListener extends AbstractMSDynamicsEventListener<StockFetchEvent, StockEventParam, Integer> {

	public StockFetchEventListener(IntegrationService integrationService) {
		super(integrationService);
	}
	
	
	
	
	

	@Override
	protected Mono<Integer> handleEventAsync(EventInfo<StockEventParam> event) {
		String extVariantId = 
					ofNullable(event)
						.map(info -> info.getEventData())
						.map(StockEventParam::getVariantId)
						.orElse(null);
		String extShopId = 
					ofNullable(event)
						.map(info -> info.getEventData())
						.map(StockEventParam::getShopId)
						.orElse(null);
		
		
		Long orgId = event.getOrganizationId();
		return getWebClient(orgId)
				.getProductById(extVariantId)
				.flatMap(this::throwExceptionIfNotOk)
				.flatMap(res -> res.bodyToMono(ProductsResponse.class))
				.flatMap(product -> getStockQtyForShop(product, extShopId));
	}
	
	
	
	
	

	private Mono<Integer> getStockQtyForShop(ProductsResponse product, String extShopId) {
		return ofNullable(product)
				.map(ProductsResponse::getProducts)
				.orElse(emptyList())
				.stream()
				.findFirst()
				.map(Product::getStocks)
				.orElse(emptyList())
				.stream()
				.filter(Objects::nonNull)
				.filter(stocks -> isNotBlankOrNull(stocks.getStoreCode()))
				.filter(stocks -> Objects.equals(extShopId, stocks.getStoreCode()))
				.findFirst()
				.map(Stocks::getValue)
				.map(BigDecimal::intValue)
				.map(i -> Mono.just(i))
				.orElse(Mono.empty());				
	}






	@Override
	protected StockFetchEvent handleError(StockFetchEvent event, Throwable t) {
		return event;
	}

}
