package com.nasnav.integration.sallab;

import static com.nasnav.integration.sallab.ElSallabIntegrationParams.AUTH_GRANT_TYPE;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.CLIENT_ID;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.CLIENT_SECRET;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.PASSWORD;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.USERNAME;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Optional;

import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.IntegrationImportedProducts;
import com.nasnav.integration.events.ProductsImportEvent;
import com.nasnav.integration.events.ShopImportedProducts;
import com.nasnav.integration.events.data.ProductImportEventParam;
import com.nasnav.integration.sallab.webclient.SallabWebClient;
import com.nasnav.integration.sallab.webclient.dto.AuthenticationData;
import com.nasnav.integration.sallab.webclient.dto.AuthenticationResponse;
import com.nasnav.integration.sallab.webclient.dto.ProductsResponse;

import reactor.core.publisher.Mono;

public class ProductImportEventListener extends AbstractElSallabEventListener<ProductsImportEvent, ProductImportEventParam, IntegrationImportedProducts> {

	public ProductImportEventListener(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	
	
	@Override
	protected Mono<IntegrationImportedProducts> handleEventAsync(EventInfo<ProductImportEventParam> event) {
		Long orgId = event.getOrganizationId();
		
		ProductImportEventParam param = event.getEventData();
		AuthenticationData authData = getAuthData(orgId);
		
		SallabWebClient client = getWebClient(orgId);
		
		client.authenticate(authData)
		 	.flatMap(this::throwExceptionIfNotOk)
		 	.flatMap(res -> res.bodyToMono(AuthenticationResponse.class))
        	.flatMap(res -> client.getProducts(res.getAccessToken()))
        	.flatMap(prodRes -> prodRes.bodyToMono(ProductsResponse.class))
        	.map(this::toIntegrationImportedProducts);
		
		return null;
	}

	
	
	
	
	
	private IntegrationImportedProducts toIntegrationImportedProducts(ProductsResponse productResponse) {
		IntegrationImportedProducts imported = new IntegrationImportedProducts();
		
		imported.setTotalPages( calculateTotalPages(productResponse) );
		imported.setAllShopsProducts( getAllShopsProducts(productResponse));
		
		return imported;
	}
	
	
	
	
	private List<ShopImportedProducts> getAllShopsProducts(ProductsResponse productResponse) {
		// TODO Auto-generated method stub
		return null;
	}






	private Integer  calculateTotalPages(ProductsResponse productResponse) {
		Integer totalProducts = ofNullable(productResponse)
									.map(ProductsResponse::getTotalSize)
									.orElse(0);
		Integer productsPerPage = ofNullable(productResponse)
									.map(ProductsResponse::getRecords)
									.map(List::size)
									.orElse(1);
		
		return (int) Math.ceil( totalProducts.doubleValue()/ productsPerPage.doubleValue());
	}






	private AuthenticationData getAuthData(Long orgId) {
		
		String grantType = integrationService.getIntegrationParamValue(orgId, AUTH_GRANT_TYPE.getValue());
		String clientId = integrationService.getIntegrationParamValue(orgId, CLIENT_ID.getValue());
		String clientSecret = integrationService.getIntegrationParamValue(orgId, CLIENT_SECRET.getValue());
		String username = integrationService.getIntegrationParamValue(orgId, USERNAME.getValue());
		String password = integrationService.getIntegrationParamValue(orgId, PASSWORD.getValue());
		
		return new AuthenticationData(grantType, clientId, clientSecret, username, password);
	}






	@Override
	protected ProductsImportEvent handleError(ProductsImportEvent event, Throwable t) {
		// TODO Auto-generated method stub
		return null;
	}

}
