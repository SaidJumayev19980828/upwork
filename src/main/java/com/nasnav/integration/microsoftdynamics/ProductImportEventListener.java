package com.nasnav.integration.microsoftdynamics;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.logging.Level.SEVERE;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.IntegrationImportedProducts;
import com.nasnav.integration.events.ProductsImportEvent;
import com.nasnav.integration.events.ShopImportedProducts;
import com.nasnav.integration.events.data.ProductImportEventParam;
import com.nasnav.integration.microsoftdynamics.webclient.dto.CategoriesResposne;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Category;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Product;
import com.nasnav.integration.microsoftdynamics.webclient.dto.ProductsResponse;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Stocks;
import com.nasnav.integration.model.ProductImportDTOWithShop;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class ProductImportEventListener extends AbstractMSDynamicsEventListener<ProductsImportEvent, ProductImportEventParam, IntegrationImportedProducts> {

	public ProductImportEventListener(IntegrationService integrationService) {
		super(integrationService);		
	}

	
	
	
	
	@Override
	protected Mono<IntegrationImportedProducts> handleEventAsync(EventInfo<ProductImportEventParam> event) {
		
		ProductImportEventParam param = event.getEventData();
		
		Mono<ProductsData> productsData =  
				getWebClient(event.getOrganizationId())
				.getProducts(param.getPageCount(), param.getPageNum())
				.flatMap(this::throwExceptionIfNotOk)
				.flatMap(res -> res.bodyToMono(ProductsResponse.class))
				.map(res -> new ProductsData(res, event.getOrganizationId()));
		
		Flux<CategoriesResposne> categories = 
				getWebClient(event.getOrganizationId())
				.getCategories()
				.flatMap(this::throwExceptionIfNotOk)
				.flatMapMany(res -> res.bodyToFlux(CategoriesResposne.class));
		
		 return Mono.zip(productsData, categories.buffer().single() , this::toIntegrationImportedProducts);
	}

	
	
	
	@Override
	protected ProductsImportEvent handleError(ProductsImportEvent event, Throwable t) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	
	private IntegrationImportedProducts toIntegrationImportedProducts(ProductsData responseWithOrgId, List<CategoriesResposne> categories) {
		Map<String,Set<String>> categoriesParents = createCategoriesParentsMap(categories);
		
		ProductsResponse response = responseWithOrgId.getProductResponse();
		Long orgId = responseWithOrgId.getOrgId();
		List<ShopImportedProducts> allShopsProducts = getAllShopsProducts(response, orgId, categoriesParents);
		
		IntegrationImportedProducts importedProducts = new IntegrationImportedProducts();
		importedProducts.setTotalPages(response.getTotalPages() -1 );
		importedProducts.setAllShopsProducts(allShopsProducts);
		
		return importedProducts;
	}





	private Map<String, Set<String>> createCategoriesParentsMap(List<CategoriesResposne> categories) {
		Map<Long, Category> cache =
			categories
			.stream()
			.map(CategoriesResposne::getCategories)
			.flatMap(List::stream)
			.collect(toMap(Category::getCategoryId, category -> category));
		
		return cache
				.values()
				.stream()
				.map(category -> getCategoryParents(category, cache))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toMap(CategoryParents::getCategoryName, CategoryParents::getParents));
	}
	
	
	
	
	private Optional<CategoryParents> getCategoryParents(Category category, Map<Long, Category> categoriesCache) {
		if(hasNoParent(category)) {
			return Optional.empty();
		}
		
		Category thisCategoryParent = categoriesCache.get(category.getParentCategory());
		if(isNull(thisCategoryParent)) {
			logger.log(SEVERE, format("FAILED To GET CATEGORY PARENT! NO CATEGORY EXISTS WITH ID [%d]", category.getParentCategory()));
			return Optional.empty();
		}
		
		Set<String> allParents = setOf(thisCategoryParent.getCategoryName());
		getCategoryParents(thisCategoryParent,categoriesCache)
			.map(CategoryParents::getParents)
			.orElse(new HashSet<>())
			.forEach(allParents::add);
		return Optional.of(new CategoryParents(category, allParents));
	}





	private boolean hasNoParent(Category category) {
		return Objects.equals(category.getLevel(), 1) || Objects.equals(category.getParentCategory(), 0L);
	}





	private List<ShopImportedProducts> getAllShopsProducts(ProductsResponse response, Long orgId, Map<String, Set<String>> categoriesParents) {
		List<ShopImportedProducts>  allShopsProducts= 
				ofNullable(response)
					.map(ProductsResponse::getProducts)
					.orElse(emptyList())
					.stream()
					.filter(Objects::nonNull)
					.map(product -> toListOfProductImportDTOWithShop(product, orgId, categoriesParents))
					.flatMap(List::stream)
					.collect( 
							groupingBy(ProductImportDTOWithShop::getExternalShopId
									, mapping(ProductImportDTOWithShop::getProductDto, toList())))
					.entrySet()
					.stream()
					.map(shopProductMapping -> new ShopImportedProducts(shopProductMapping.getKey(), shopProductMapping.getValue()))
					.collect(toList());
		return allShopsProducts;
	}
	
	
	
	
	
	private List<ProductImportDTOWithShop> toListOfProductImportDTOWithShop(Product product, Long orgId, Map<String, Set<String>> categoriesParents) {
		return ofNullable(product)
				.map(Product::getStocks)
				.orElse(emptyList())
				.stream()
				.filter(this::isValidStock)
				.map(stock -> toProductImportDTOWithShop(product, stock, orgId, categoriesParents))
				.collect(toList());
	}





	private boolean isValidStock(Stocks stock) {
		return isNotBlankOrNull(stock.getStoreCode()) 
				&& stock.getValue() != null;
	} 
	
	
	
	
	private ProductImportDTOWithShop toProductImportDTOWithShop(Product product, Stocks stock, Long orgId, Map<String, Set<String>> categoriesParents) {
		ProductImportDTOWithShop importDto = new ProductImportDTOWithShop();
		String externalShopId = stock.getStoreCode();
		
		ProductImportDTO productImportDto = toProductImportDto(product, stock, categoriesParents);
		
		importDto.setExternalShopId(externalShopId);
		importDto.setProductDto(productImportDto);
		
		return importDto;
	}





	private ProductImportDTO toProductImportDto(Product product, Stocks stock, Map<String, Set<String>> categoriesParents) {
		Integer stockQty =
				ofNullable(stock.getValue())
					.map(BigDecimal::intValue)
					.orElse(0);
		
		Set<String> tags = createTagsList(product, categoriesParents);
		
		ProductImportDTO productImportDto = new ProductImportDTO();
		productImportDto.setBarcode(product.getSku());
		productImportDto.setBrand(product.getBrand());
		productImportDto.setTags( tags);
		productImportDto.setDescription(product.getItemDescription());
		productImportDto.setExternalId(product.getAxId());
		productImportDto.setName(product.getName());
		productImportDto.setPrice(product.getOriginalPrice());		
		productImportDto.setQuantity(stockQty);
		
		return productImportDto;
	}





	private Set<String> createTagsList(Product product, Map<String, Set<String>> categoriesParents) {
		Set<String> tags = setOf(product.getCategory(), product.getBrand());
		Set<String> categoryParents = 
				ofNullable(product.getCategory())
				.map(categoriesParents::get)
				.orElse(emptySet());
		tags.addAll(categoryParents);
		return tags;
	}
	

}



@Data
@AllArgsConstructor
class ProductsData{
	private ProductsResponse productResponse;
	private Long orgId;	
}



@Data
@AllArgsConstructor
class CategoryParents{
	private Long categoryId;
	private String categoryName;
	private Set<String> parents;
	
	public CategoryParents(Category category, Set<String> parents) {
		this.categoryId = category.getCategoryId();
		this.categoryName = category.getCategoryName();
		this.parents = parents;
	};
}
