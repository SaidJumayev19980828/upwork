package com.nasnav.integration.sallab;

import static com.nasnav.commons.utils.EntityUtils.setOf;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.AUTH_GRANT_TYPE;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.CLIENT_ID;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.CLIENT_SECRET;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.PASSWORD;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.USERNAME;
import static java.math.BigDecimal.ZERO;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static reactor.core.scheduler.Schedulers.elastic;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
					.map(this::toIntegrationImportedProducts);
				
	}
	
	
	
	
	
	
	
	private Mono<FetchedProductsData>  fetchProductsFromExternalSystem(ProductImportEventParam param, String authToken, Long orgId) {
		SallabWebClient client = getWebClient(orgId);

		return 
				client
				.getProducts(authToken)
				.flatMap(this::throwExceptionIfNotOk)
				.flatMap(prodRes -> prodRes.bodyToMono(ProductsResponse.class))
				.map(prodRes -> toFetchedProductsData(prodRes, param, orgId, authToken));
	}
	
	
	
	
	
	
	private FetchedProductsData toFetchedProductsData(ProductsResponse productsResponse, ProductImportEventParam param, Long orgId, String authToken) {
		Integer totalPages = calculateTotalPages(productsResponse);		
		List<Product> page = getProductsData(productsResponse, param, orgId);
		
		return new FetchedProductsData(totalPages, page, orgId, authToken);
	}






	private List<Product> getProductsData(ProductsResponse productsResponse, ProductImportEventParam param,
			Long orgId) {
		List<Product> productsBuffer = new ArrayList<>(productsResponse.getRecords().size());
		
		getMoreProductsIfNeeded(productsBuffer , param, productsResponse, orgId);

		return getProductPage(productsBuffer, param);
	}



private List<Product> getProductPage(List<Product> allNeededProducts, ProductImportEventParam param) {
		Integer fromIndex = param.getPageCount()*(param.getPageNum() -1);
		Integer toIndex = fromIndex + param.getPageCount();
		toIndex = toIndex > (allNeededProducts.size() -1) ? allNeededProducts.size() -1 : toIndex;
		
		return allNeededProducts.subList(fromIndex, toIndex);
	}






//TODO : this should be converted to a reactive style, but i couldn't think of something at the time. and we are blocking any way to get all
	//products after processing the event.
	/**
	 * El sallab api returns products in linked-list style, each response provides a batch of products and  an url for the next batch.
	 * so, we need to traverse and buffer products until we get the required page.
	 * */
	private void getMoreProductsIfNeeded(List<Product> buffer, ProductImportEventParam param, ProductsResponse response, Long orgId) {
		SallabWebClient client = getWebClient(orgId);		
		
		buffer.addAll(getProductsList(response));		
		
		Integer neededBufferSize = param.getPageCount()*param.getPageNum();
		
		if( !response.getDone() && neededBufferSize < buffer.size()) {
			ProductsResponse nextResponse=
					client
						.getProductsNextRecords("", response.getNextRecordsUrl())
						.flatMap(this::throwExceptionIfNotOk)
						.flatMap(prodRes -> prodRes.bodyToMono(ProductsResponse.class))
						.block();
			
			getMoreProductsIfNeeded(buffer, param, nextResponse, orgId);	
		}
	}
	




	private List<Product> getProductsList(ProductsResponse productsResponse) {
		List<Product> allNeededProducts = 
				productsResponse
					.getRecords()
					.stream()
					.map(Record::getProduct)
					.collect(toList());
		return allNeededProducts;
	}
	
	
	
	






	private IntegrationImportedProducts toIntegrationImportedProducts(FetchedProductsData productsData) {
		IntegrationImportedProducts imported = new IntegrationImportedProducts();
		
		imported.setTotalPages( productsData.getTotalPages() );
		imported.setAllShopsProducts( getAllShopsProducts(productsData));
		
		return imported;
	} 
	
	
	
	
	
	
	
	//TODO need a generic method for this for a util class
	private List<ShopImportedProducts> getAllShopsProducts(FetchedProductsData productsData) {
		
		List<ProductImportDTOWithShop> productsWithShops = toProductImportDtoWithShop(productsData);
		return productsWithShops
				.stream()
				.filter(prod -> Objects.nonNull(prod.getExternalShopId()))
				.collect( 
						groupingBy(ProductImportDTOWithShop::getExternalShopId
								, mapping(ProductImportDTOWithShop::getProductDto, toList())))
				.entrySet()
				.stream()
				.map(shopProductMapping -> new ShopImportedProducts(shopProductMapping.getKey(), shopProductMapping.getValue()))
				.collect(toList());
	}





	//TODO: should be some how in a base class of all product import events handlers
	private List<ProductImportDTOWithShop> toProductImportDtoWithShop(FetchedProductsData productsData) {
		
		return 
			Flux.fromIterable(productsData.getProducts())
				.parallel()
				.runOn(elastic())
				.flatMap(product -> getProductWithQtyAndStock(product, productsData.getOrgId()))
				.flatMap(this::toManyProductImportDtoWithShop)
				.ordered(comparing(ProductImportDTOWithShop::getExternalShopId))
				.buffer()
				.blockFirst();
	}
	
	
	
	
	
	private Flux<ProductImportDTOWithShop> toManyProductImportDtoWithShop(ProductWithQtyAndPrice productWithQtyAndPrice) {
		return 
				Flux.fromStream(
					productWithQtyAndPrice
					.getStocks()
					.stream()
					.map(stock -> toProductImportDTOWithShop(productWithQtyAndPrice, stock))
				);
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
		dto.setBrand(product.getFactory());
		dto.setExternalId(product.getItemNoC());
		dto.setName(product.getModel());
		dto.setPrice(price);
		dto.setQuantity(quantity);
		dto.setDescription(product.getDescription());
		dto.setFeatures(variantFeatures);
		dto.setTags(tags);
		dto.setExtraAttributes(extraAttributes);
		
		return dto;
	}






	private Integer getQuantity(ItemStockBalance stock) {
		Integer quantity = ofNullable(stock)
							.map(ItemStockBalance::getQuantity)
							.map(BigDecimal::intValue)
							.orElse(0);
		return quantity;
	}






	private BigDecimal getPrice(ProductWithQtyAndPrice productWithQtyAndPrice) {
		BigDecimal price = ofNullable(productWithQtyAndPrice)
							.map(ProductWithQtyAndPrice::getPrice)
							.map(ItemPrice::getNetPrice)
							.orElse(ZERO);
		return price;
	}






	private Set<String> getTags(Product product) {
		Set<String> tags = setOf(product.getCategory(), product.getFamily(), product.getType());
		return tags;
	}






	private Map<String, String> getVariantFeatures(Product product) {
		Map<String, String> variantFeatures = 
				MapBuilder
				.<String,String>map()
				.put("Color", product.getColor())
				.put("Size", product.getSize())
				.put("Class", product.getClassC())
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
					.single();
		
		return Mono.zip(ProductWithQtyAndPrice::new, Mono.just(product), price, stocks, Mono.just(orgId));
	}

	
	





	private Integer  calculateTotalPages(ProductsResponse productsResponse) {
		Integer totalProducts = ofNullable(productsResponse)
									.map(ProductsResponse::getTotalSize)
									.orElse(0);
		Integer productsPerPage = ofNullable(productsResponse)
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
