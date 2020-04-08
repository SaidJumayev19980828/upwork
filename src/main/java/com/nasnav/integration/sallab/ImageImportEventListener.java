package com.nasnav.integration.sallab;

import static com.nasnav.commons.utils.EntityUtils.anyIsNonNull;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_INVALID_PAGINATION_PARAMS;
import static com.nasnav.integration.enums.IntegrationParam.IMG_SERVER_URL;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.web.reactive.function.BodyExtractors.toDataBuffers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.ImagesImportEvent;
import com.nasnav.integration.events.data.ImageImportParam;
import com.nasnav.integration.events.data.ImportedImagesUrlMappingPage;
import com.nasnav.integration.sallab.webclient.SallabWebClient;
import com.nasnav.integration.sallab.webclient.dto.AuthenticationData;
import com.nasnav.integration.sallab.webclient.dto.AuthenticationResponse;
import com.nasnav.integration.sallab.webclient.dto.Product;
import com.nasnav.integration.sallab.webclient.dto.ProductsResponse;
import com.nasnav.service.model.VariantIdentifier;
import com.nasnav.service.model.VariantIdentifierAndUrlPair;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class ImageImportEventListener extends AbstractElSallabEventListener<ImagesImportEvent, ImageImportParam, ImportedImagesUrlMappingPage> {

	private static final String IMG_URL_TEMPLATE = "/services/data/v36.0/sobjects/Attachment/%s/Body";
	
	
	
	public ImageImportEventListener(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	@Override
	protected Mono<ImportedImagesUrlMappingPage> handleEventAsync(EventInfo<ImageImportParam> event) {
		Long orgId = event.getOrganizationId();
		
		ImageImportParam param = event.getEventData();
		
		SallabWebClient client = getWebClient(orgId);
		AuthenticationData authData = getImgServerAuthData(orgId);
		
		return client
				.authenticateImgServer(authData)
			 	.flatMap(this::throwExceptionIfNotOk)
			 	.flatMap(res -> res.bodyToMono(AuthenticationResponse.class))
			 	.map(res -> getImgWebClient(res, orgId))
			 	.flatMap(imgClient -> getProductAndClientPair(param, imgClient, orgId))
			 	.map(pair -> getUrlToProductMappingPage((FetchedProductsDataWithTotal)pair.getProducts(), pair.getClient()));
	}
	
	
	
	
	private Mono<FetchedProductsDataAndWebClientPair> getProductAndClientPair(
			 		ImageImportParam param
			 		, WebClientWithAuthToken imgClient
			 		, Long orgId){
		SallabWebClient client = getWebClient(orgId);
		AuthenticationData authData = getAuthData(orgId);
		
		Mono<FetchedProductsData> productsMono = 
			client
			.authenticate(authData)
			.flatMap(this::throwExceptionIfNotOk)
		 	.flatMap(res -> res.bodyToMono(AuthenticationResponse.class))
			.map(AuthenticationResponse::getAccessToken)
			.flatMap(authToken -> fetchProductsFromExternalSystem(param, authToken, orgId));
		
		return Mono.zip( productsMono, Mono.just(imgClient.getClient())
				 	, (prod, imgWebClient) -> new FetchedProductsDataAndWebClientPair(prod, imgWebClient));
	}
	
	
	
	
	private WebClientWithAuthToken getImgWebClient(AuthenticationResponse response, Long orgId) {
		String token = response.getAccessToken();
		return new WebClientWithAuthToken(token, buildImageWebClient(orgId, token));
	}
	
	
	
	private ImportedImagesUrlMappingPage getUrlToProductMappingPage(FetchedProductsDataWithTotal products, WebClient client){
		Integer total = products.getTotalElements();
		Map<String, List<VariantIdentifier>> fileIdentifiersMap = getUrlToProductMapping(products);
		
		return new ImportedImagesUrlMappingPage(total , fileIdentifiersMap, client);
	}
	
	
	
	




	private Map<String, List<VariantIdentifier>> getUrlToProductMapping(FetchedProductsData products) {
		Long orgId = products.getOrgId();
		String baseUrl = integrationService.getIntegrationParamValue(orgId, IMG_SERVER_URL.getValue());
		return 
				products
				  .getProducts()
				  .stream()
				  .map(product -> toVariantIdentifierAndUrlPair(product, baseUrl))
				  .filter(this::isValidVariantIdentifierAndUrlPair)
				  .collect( toMap(VariantIdentifierAndUrlPair::getUrl
						    		 , VariantIdentifierAndUrlPair::getIdentifier
						    		 , (list1, list2) -> concat(list1.stream(), list2.stream()).collect(toList())
						    		 ));
	}

	
	
	
	private boolean isValidVariantIdentifierAndUrlPair(VariantIdentifierAndUrlPair pair) {
		return noneIsNull(pair, pair.getUrl(), pair.getIdentifier()) 
				&& pair.getIdentifier()
						.stream()
						.allMatch(ids -> anyIsNonNull(ids.getBarcode(), ids.getExternalId(), ids.getVariantId()));
	}
	
	
	
	
	
	private VariantIdentifierAndUrlPair toVariantIdentifierAndUrlPair(Product product, String baseUrl) {
		VariantIdentifier ids = getIdentifiers(product);
		String imgUrl = createImgUrl(product, baseUrl);
		return new VariantIdentifierAndUrlPair(imgUrl, asList(ids));
	}


	
	

	private String createImgUrl(Product product, String baseUrl) {
		return format(baseUrl + IMG_URL_TEMPLATE, product.getIconAttachmentId());
	}


	
	


	private VariantIdentifier getIdentifiers(Product product) {
		return new VariantIdentifier(null, product.getItemNoC(), product.getItemNoC());
	}




	private Mono<FetchedProductsData>  fetchProductsFromExternalSystem(ImageImportParam param, String authToken, Long orgId) {
		SallabWebClient client = getWebClient(orgId);
		return 
				client
				.getProducts(authToken)
				.flatMap(this::throwExceptionIfNotOk)
				.flatMap(prodRes -> prodRes.bodyToMono(ProductsResponse.class))
				.flatMap(prodRes -> toFetchedProductsData(prodRes, param, orgId, authToken));
	}

	
	
	
	
	private Mono<FetchedProductsData> toFetchedProductsData(ProductsResponse productsResponse, ImageImportParam param, Long orgId, String authToken) {
		Mono<Integer> totalPages = calculateTotalPages(productsResponse, param);		
		Mono<ProductPage> page = getProductsData(productsResponse, param, orgId, authToken);
		
		return Mono.zip(totalPages
						, page
						, (totPages, productsPage) -> new FetchedProductsDataWithTotal(totPages, productsPage, orgId, authToken) );
	}






	private Mono<ProductPage> getProductsData(ProductsResponse productsResponse, ImageImportParam param,
			Long orgId, String authToken) {
		
		Flux<Product> allProducts = getAllRequiredProducts(param, productsResponse, orgId, authToken);

		return getProductPageMono(allProducts, param, productsResponse.getTotalSize());
	}
	



	private Mono<ProductPage> getProductPageMono(Flux<Product> allNeededProducts, ImageImportParam param, Integer total) {
		return allNeededProducts
				.buffer()
				.single()
				.map( bufferedProducts -> getProductPage(bufferedProducts, param))
				.map( pageProducts -> new ProductPage(total, pageProducts));
	}
	
	
	
	
	//TODO: pagination is disabled for now, should be done later
	private List<Product> getProductPage(List<Product> allNeededProducts, ImageImportParam param) {
		Integer fromIndex = param.getPageCount()*(param.getPageNum() -1);
		Integer toIndex = fromIndex + param.getPageCount();
		toIndex = toIndex > allNeededProducts.size() ? allNeededProducts.size() : toIndex;
		
		if(isInvalidCalculatedIndices(fromIndex, toIndex, allNeededProducts.size())) {
			throw new RuntimeBusinessException(
					format(ERR_INVALID_PAGINATION_PARAMS, param.toString(), fromIndex, toIndex)
					, "INVALID PARAMS: page_count, page_num"
					, NOT_ACCEPTABLE);
		}
		
		return allNeededProducts.subList(fromIndex, toIndex);
	}






	private boolean isInvalidCalculatedIndices(Integer fromIndex, Integer toIndex, int bufferSize) {
		return fromIndex > toIndex || toIndex > bufferSize;
	}




	/**
	 * El sallab api returns products in linked-list style, each response provides a batch of products and  an url for the next batch.
	 * so, we need to traverse and buffer products until we get the required page.
	 * */
	private Flux<Product> getAllRequiredProducts(ImageImportParam param, ProductsResponse response, Long orgId, String authToken) {
		return getRemainingProductsFromApiIfNeeded(param, new AccumlatedProductsResponse(response, orgId, authToken))
					.flatMapIterable(accProducts -> accProducts.getBuffer());
	}
	
	
	
	
	
	/**
	 * recursive function to get products from the API's using the linked-list style of it.
	 * */
	private Mono<AccumlatedProductsResponse> getRemainingProductsFromApiIfNeeded(ImageImportParam param, AccumlatedProductsResponse response) {
		Integer neededBufferSize = param.getPageCount()*param.getPageNum();
		
		if( !response.isDone() && neededBufferSize > response.getBuffer().size()) {
			return	getProductsUsingWebAPI(response)
						.flatMap(newRes -> getRemainingProductsFromApiIfNeeded(
												param
												, new AccumlatedProductsResponse(newRes, response)) );
		}
		
		return Mono.just(response);
	}
	
	
	
	
	private Mono<ProductsResponse> getProductsUsingWebAPI(AccumlatedProductsResponse response){
		Long orgId = response.getOrgId();
		String url = response.getResponse().getNextRecordsUrl();
		String authToken = response.getAuthToken();
		
		SallabWebClient client = getWebClient(orgId);
		return client
				.getProductsNextRecords(authToken, url)
				.flatMap(this::throwExceptionIfNotOk)
				.flatMap(prodRes -> prodRes.bodyToMono(ProductsResponse.class));				
	}
	
	
	
	
	private Mono<Integer>  calculateTotalPages(ProductsResponse productsResponse, ImageImportParam param) {
		Integer totalProducts = ofNullable(productsResponse)
									.map(ProductsResponse::getTotalSize)
									.orElse(0);
		Integer productsPerPage = param.getPageCount();
		
		return Mono.just( (int) Math.ceil( totalProducts.doubleValue()/ productsPerPage.doubleValue()));
	}
	
	
	
	
	
	private WebClient buildImageWebClient(Long orgId, String token) {
		
		return WebClient
        		.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().wiretap(true)
                ))
                .filter(contentTypeInterceptor())
                .defaultHeader("Authorization", "Bearer "+token)
                .build();
	}
	
	
	
	@Override
	protected ImagesImportEvent handleError(ImagesImportEvent event, Throwable t) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	

	/**
	 * webclient interceptor that overrides the response headers ...
	 * */
	private ExchangeFilterFunction contentTypeInterceptor() {
	    return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> 
	        Mono.just(
	        		ClientResponse
	        			.from(clientResponse)
	        			.headers(headers -> headers.remove(HttpHeaders.CONTENT_TYPE)) //override the content type
	        			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE) 
	        			.body(clientResponse.body(toDataBuffers()) ) // copy the body as bytes with no processing
	        			.build()));
	}


}




@Data
@EqualsAndHashCode(callSuper= true)
class FetchedProductsDataWithTotal extends FetchedProductsData{
	private Integer totalElements;
	
	public FetchedProductsDataWithTotal(Integer totPages, ProductPage page, Long orgId, String authToken) {
		super(totPages, page.getProducts(), orgId, authToken);
		this.totalElements = page.getTotal();		
	}

}




@Data
@AllArgsConstructor
class ProductPage{
	private Integer total;
	private List<Product> products;
}




@Data
@AllArgsConstructor
class WebClientWithAuthToken {
	private String authToken;
	private WebClient client;
}




@Data
@AllArgsConstructor
class FetchedProductsDataAndWebClientPair{
	private FetchedProductsData products;
	private WebClient client;
}
