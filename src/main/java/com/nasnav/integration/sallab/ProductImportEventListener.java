package com.nasnav.integration.sallab;

import static com.nasnav.commons.utils.EntityUtils.setOf;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.AUTH_GRANT_TYPE;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.CLIENT_ID;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.CLIENT_SECRET;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.PASSWORD;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.USERNAME;
import static com.nasnav.integration.sallab.ShopsImportEventListener.HARD_CODED_STOCK;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static reactor.core.scheduler.Schedulers.elastic;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.utils.MapBuilder;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.IntegrationImportedProducts;
import com.nasnav.integration.events.ProductsImportEvent;
import com.nasnav.integration.events.ShopImportedProducts;
import com.nasnav.integration.events.data.ProductImportEventParam;
import com.nasnav.integration.model.ProductImportDTOWithShop;
import com.nasnav.integration.sallab.webclient.SallabWebClient;
import com.nasnav.integration.sallab.webclient.dto.AuthenticationData;
import com.nasnav.integration.sallab.webclient.dto.AuthenticationResponse;
import com.nasnav.integration.sallab.webclient.dto.ItemPrice;
import com.nasnav.integration.sallab.webclient.dto.ItemSearchParam;
import com.nasnav.integration.sallab.webclient.dto.ItemStockBalance;
import com.nasnav.integration.sallab.webclient.dto.Product;
import com.nasnav.integration.sallab.webclient.dto.ProductsResponse;
import com.nasnav.integration.sallab.webclient.dto.Record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ProductImportEventListener extends AbstractElSallabEventListener<ProductsImportEvent, ProductImportEventParam, IntegrationImportedProducts> {

	private static final int PRODUCT_BATCH_PROCESSING_DELAY = 6;






	public ProductImportEventListener(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	
	
	@Override
	protected Mono<IntegrationImportedProducts> handleEventAsync(EventInfo<ProductImportEventParam> event) {
		Long orgId = event.getOrganizationId();
		
		ProductImportEventParam param = event.getEventData();
		AuthenticationData authData = getAuthData(orgId);
		
		SallabWebClient client = getWebClient(orgId);
		
		return client
				.authenticate(authData)
			 	.flatMap(this::throwExceptionIfNotOk)
			 	.flatMap(res -> res.bodyToMono(AuthenticationResponse.class))
			 	.flatMap(res -> toIntegrationImportedProductsMono(param, res.getAccessToken(), orgId));
	}

	
	
	
	
	
	private Mono<IntegrationImportedProducts> toIntegrationImportedProductsMono(ProductImportEventParam param, String authToken, Long orgId) {
		return
				fetchProductsFromExternalSystem(param, authToken, orgId)
					.flatMap(this::toIntegrationImportedProducts);
				
	}
	
	
	
	
	
	
	
	private Mono<FetchedProductsData>  fetchProductsFromExternalSystem(ProductImportEventParam param, String authToken, Long orgId) {
		SallabWebClient client = getWebClient(orgId);

		return 
				client
				.getProducts(authToken)
				.flatMap(this::throwExceptionIfNotOk)
				.flatMap(prodRes -> prodRes.bodyToMono(ProductsResponse.class))
				.flatMap(prodRes -> toFetchedProductsData(prodRes, param, orgId, authToken));
	}
	
	

	
	private Mono<FetchedProductsData> toFetchedProductsData(ProductsResponse productsResponse, ProductImportEventParam param, Long orgId, String authToken) {
		Mono<Integer> totalPages = calculateTotalPages(productsResponse, param);		
		Mono<List<Product>> page = getProductsData(productsResponse, param, orgId);
		
		return Mono.zip(totalPages
						, page
						, (totPages, products) -> new FetchedProductsData(totPages, products, orgId, authToken) );
	}






	private Mono<List<Product>> getProductsData(ProductsResponse productsResponse, ProductImportEventParam param,
			Long orgId) {
		
		Flux<Product> allProducts = getAllRequiredProducts(param, productsResponse, orgId);

		return getProductPageMono(allProducts, param);
	}
	



	private Mono<List<Product>> getProductPageMono(Flux<Product> allNeededProducts, ProductImportEventParam param) {
		return allNeededProducts
				.buffer()
				.single()
				.map( productslist -> getProductPage(productslist, param));
	}
	
	
	
	
	//TODO: pagination is disabled for now, should be done later
	private List<Product> getProductPage(List<Product> allNeededProducts, ProductImportEventParam param) {
		Integer fromIndex = param.getPageCount()*(param.getPageNum() -1);
		Integer toIndex = fromIndex + param.getPageCount();
		toIndex = toIndex > (allNeededProducts.size() -1) ? allNeededProducts.size() -1 : toIndex;
		
//		return allNeededProducts.subList(fromIndex, toIndex);
		return allNeededProducts;
	}






	/**
	 * El sallab api returns products in linked-list style, each response provides a batch of products and  an url for the next batch.
	 * so, we need to traverse and buffer products until we get the required page.
	 * */
	private Flux<Product> getAllRequiredProducts(ProductImportEventParam param, ProductsResponse response, Long orgId) {
		return getRemainingProductsFromApiIfNeeded(param, new AccumlatedProductsResponse(response), orgId)
					.flatMapIterable(accProducts -> accProducts.getBuffer());
	}
	
	
	
	
	
	/**
	 * recursive function to get products from the API's using the linked-list style of it.
	 * */
	private Mono<AccumlatedProductsResponse> getRemainingProductsFromApiIfNeeded(ProductImportEventParam param, AccumlatedProductsResponse response, Long orgId) {
		Integer neededBufferSize = param.getPageCount()*param.getPageNum();
		
		if( !response.isDone() && neededBufferSize > response.getBuffer().size()) {
			return	getProductsUsingWebAPI(orgId, response.getResponse().getNextRecordsUrl())
						.flatMap(newRes -> getRemainingProductsFromApiIfNeeded(param, new AccumlatedProductsResponse(newRes, response.getBuffer()), orgId) );
		}
		
		return Mono.just(response);
	}
	
	
	
	
	private Mono<ProductsResponse> getProductsUsingWebAPI(Long orgId, String url){
		SallabWebClient client = getWebClient(orgId);
		return client
				.getProductsNextRecords("", url)
				.flatMap(this::throwExceptionIfNotOk)
				.flatMap(prodRes -> prodRes.bodyToMono(ProductsResponse.class));				
	}
	



	private Mono<IntegrationImportedProducts> toIntegrationImportedProducts(FetchedProductsData productsData) {
		Mono<Integer> totalPages = Mono.just(productsData.getTotalPages()); 
		Mono<List<ShopImportedProducts>> shopsProducts =  getAllShopsProducts(productsData);
		
		
		return Mono.zip(totalPages, shopsProducts, (tot, prods) -> new IntegrationImportedProducts(tot, prods));
	} 
	
	
	
	
	
	
	
	//TODO need a generic method for this for a util class
	private Mono<List<ShopImportedProducts>> getAllShopsProducts(FetchedProductsData productsData) {
		
		Flux<ProductImportDTOWithShop> productsWithShops = toProductImportDtoWithShop(productsData);
		return productsWithShops
				.collect( 
						groupingBy(ProductImportDTOWithShop::getExternalShopId
								, mapping(ProductImportDTOWithShop::getProductDto, toList())))
				.flatMapIterable(Map::entrySet)
				.map(shopProductMapping -> new ShopImportedProducts(shopProductMapping.getKey(), shopProductMapping.getValue()))
				.collect(toList());
	}





	//TODO: should be some how in a base class of all product import events handlers
	private Flux<ProductImportDTOWithShop> toProductImportDtoWithShop(FetchedProductsData productsData) {
		
		return 
			Flux.fromIterable(productsData.getProducts())
				.window(10)
				.delayElements(Duration.ofSeconds(PRODUCT_BATCH_PROCESSING_DELAY))
				.flatMap(batch -> getStockAndPriceForProductBatch(batch, productsData.getOrgId()));	
	}
	
	
	
	
	private Flux<ProductImportDTOWithShop> getStockAndPriceForProductBatch(Flux<Product> flux, Long orgId){
		return flux
				.parallel()
				.runOn(elastic())
				.flatMap(product -> getProductWithQtyAndStock(product, orgId))
				.flatMap(this::toManyProductImportDtoWithShop)
				.ordered(comparing(ProductImportDTOWithShop::getExternalShopId));
	}
	
	
	
	
	
	private Flux<ProductImportDTOWithShop> toManyProductImportDtoWithShop(ProductWithQtyAndPrice productWithQtyAndPrice) {
		BigDecimal totalQty = 
			productWithQtyAndPrice
				.getStocks()
				.stream()
				.map(ItemStockBalance::getQuantity)
				.reduce(ZERO, BigDecimal::add);
		
		ItemStockBalance totalStock = new ItemStockBalance(); 
		totalStock.setStockId(HARD_CODED_STOCK);
		totalStock.setQuantity(totalQty);
		
		return 	Flux.fromIterable( asList(toProductImportDTOWithShop(productWithQtyAndPrice, totalStock)) );
	}
	
	
	
	
	
	private ProductImportDTOWithShop toProductImportDTOWithShop(ProductWithQtyAndPrice productWithQtyAndPrice, ItemStockBalance stock) {
		String externalShopId = String.valueOf(stock.getStockId());
		ProductImportDTO productDto = toProductImportDTO(productWithQtyAndPrice, stock);
		return new ProductImportDTOWithShop(productWithQtyAndPrice.getOrgId(), externalShopId, productDto);
	}
	
	
	
	
	
	
	private ProductImportDTO toProductImportDTO(ProductWithQtyAndPrice productWithQtyAndPrice, ItemStockBalance stock) {
		ProductImportDTO dto = new ProductImportDTO();
		Product product = productWithQtyAndPrice.getProduct();
		BigDecimal price = getPrice(productWithQtyAndPrice);
		Integer quantity = getQuantity(stock);
		Map<String, String> variantFeatures = getVariantFeatures(product);
		Set<String> tags = getTags(product);
		Map<String, String> extraAttributes = getExtraAttributes(product);
		
		
		dto.setBarcode(product.getItemNoC());
		dto.setBrand(product.getEnglishFactory());
		dto.setExternalId(product.getItemNoC());
		dto.setName(product.getEnglishModel());
		dto.setPrice(price);
		dto.setQuantity(quantity);
		dto.setDescription(product.getDescription());
		dto.setFeatures(variantFeatures);
		dto.setTags(tags);
		dto.setExtraAttributes(extraAttributes);
		
		return dto;
	}






	private Integer getQuantity(ItemStockBalance stock) {
		return ofNullable(stock)
				.map(ItemStockBalance::getQuantity)
				.map(BigDecimal::intValue)
				.orElse(0);
	}






	private BigDecimal getPrice(ProductWithQtyAndPrice productWithQtyAndPrice) {
		return ofNullable(productWithQtyAndPrice)
				.map(ProductWithQtyAndPrice::getPrice)
				.map(ItemPrice::getNetPrice)
				.orElse(ZERO);
	}






	private Set<String> getTags(Product product) {
		Set<String> tags = setOf(product.getEnglishCategory(), product.getEnglishFamily(), product.getEnglishType());
		return tags;
	}






	private Map<String, String> getVariantFeatures(Product product) {
		Map<String, String> variantFeatures = 
				MapBuilder
				.<String,String>map()
				.put("Color", product.getEnglishColor())
				.put("Size", product.getSize())
				.put("Class", product.getEnglishClass())
				.getMap();
		return variantFeatures;
	}






	private Map<String, String> getExtraAttributes(Product product) {
		Map<String, String> extraAttributes = 
				MapBuilder
				.<String,String>map()
				.put("Cut", product.getCut())
				.put("Origin", product.getEnglishOrigin())
				.put("Product Code", product.getProductCode())
				.put("Style", product.getEnglishStyle())
				.put("Glaze", product.getEnglishGlaze())
				.getMap();
		return extraAttributes;
	}






	private Mono<ProductWithQtyAndPrice> getProductWithQtyAndStock(Product product, Long orgId){
		SallabWebClient client = getWebClient(orgId);
		Mono<ItemPrice> price =  
				client
					.getItemPrice(new ItemSearchParam(product.getItemNoC()))
					.flatMap(this::throwExceptionIfNotOk)
					.flatMap(res -> res.bodyToMono(ItemPrice.class));
		Mono<List<ItemStockBalance>> stocks =  
				client
					.getItemStockBalance(product.getItemNoC(), LocalDate.now().getYear())
					.flatMap(this::throwExceptionIfNotOk)
					.flatMapMany(stockRes -> stockRes.bodyToFlux(ItemStockBalance.class))
					.buffer()
					.defaultIfEmpty(emptyList())
					.single();
		
		return Mono.zip(ProductWithQtyAndPrice::new, Mono.just(product), price, stocks, Mono.just(orgId));
	}

	
	





	private Mono<Integer>  calculateTotalPages(ProductsResponse productsResponse, ProductImportEventParam param) {
		Integer totalProducts = ofNullable(productsResponse)
									.map(ProductsResponse::getTotalSize)
									.orElse(0);
		Integer productsPerPage = param.getPageCount();
		
		return Mono.just( (int) Math.ceil( totalProducts.doubleValue()/ productsPerPage.doubleValue()));
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








@Data
@AllArgsConstructor
@NoArgsConstructor
class FetchedProductsData{
	private Integer totalPages;
	private List<Product> products;	
	private Long orgId;
	private String authToken;
}




@Data
@AllArgsConstructor
class ProductWithQtyAndPrice{
	private Product product;
	private ItemPrice price;
	private List<ItemStockBalance> stocks;
	private Long orgId;
	
	
	@SuppressWarnings("unchecked")
	public ProductWithQtyAndPrice(Object... args) {
		this.product = (Product)args[0];
		this.price = (ItemPrice)args[1];
		this.stocks = (List<ItemStockBalance>)args[2];
		this.orgId = (Long) args[3];
	} 
}




@Data
class AccumlatedProductsResponse{
	private ProductsResponse response;
	private List<Product> buffer;
	
	
	
	
	public AccumlatedProductsResponse(ProductsResponse response) {
		this.response = response;
		this.buffer = new ArrayList<>(response.getRecords().size());
		buffer.addAll(getProductsList(response));
	}
	
	
	
	public AccumlatedProductsResponse(ProductsResponse response, List<Product> bufferedData) {
		this.response = response;
		List<Product> newData = getProductsList(response);
		
		this.buffer = new ArrayList<>(bufferedData.size() + newData.size());
		this.buffer.addAll(bufferedData);
		this.buffer.addAll(newData);
	}
	
	
	
	private List<Product> getProductsList(ProductsResponse productsResponse) {
		return productsResponse
					.getRecords()
					.stream()
					.map(Record::getProduct)
					.collect(toList());
	}
	
	
	
	public Boolean isDone() {
		return response.getDone();
	}
	
	
}
