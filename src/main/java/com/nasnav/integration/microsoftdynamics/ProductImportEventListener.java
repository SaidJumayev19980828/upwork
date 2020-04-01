package com.nasnav.integration.microsoftdynamics;

import static com.nasnav.commons.utils.EntityUtils.setOf;
import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.OK;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.IntegrationImportedProducts;
import com.nasnav.integration.events.ProductsImportEvent;
import com.nasnav.integration.events.ShopImportedProducts;
import com.nasnav.integration.events.data.ProductImportEventParam;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Product;
import com.nasnav.integration.microsoftdynamics.webclient.dto.ProductsResponse;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Stocks;
import com.nasnav.integration.model.ProductImportDTOWithShop;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Mono;


public class ProductImportEventListener extends AbstractMSDynamicsEventListener<ProductsImportEvent, ProductImportEventParam, IntegrationImportedProducts> {

	public ProductImportEventListener(IntegrationService integrationService) {
		super(integrationService);		
	}

	
	
	
	
	@Override
	protected Mono<IntegrationImportedProducts> handleEventAsync(EventInfo<ProductImportEventParam> event) {
		
		ProductImportEventParam param = event.getEventData();
		
		return getWebClient(event.getOrganizationId())
				.getProducts(param.getPageCount(), param.getPageNum())
				.filter( res -> res.statusCode() == OK)
				.flatMap(this::throwExceptionIfNotOk)
				.flatMap(res -> res.bodyToMono(ProductsResponse.class))
				.map(res -> new ProductResponseWithOrgId(res, event.getOrganizationId()))
				.map(this::toIntegrationImportedProducts);
	}

	
	
	
	@Override
	protected ProductsImportEvent handleError(ProductsImportEvent event, Throwable t) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	
	private IntegrationImportedProducts toIntegrationImportedProducts(ProductResponseWithOrgId responseWithOrgId) {
		ProductsResponse response = responseWithOrgId.getProductResponse();
		Long orgId = responseWithOrgId.getOrgId();
		List<ShopImportedProducts> allShopsProducts = getAllShopsProducts(response, orgId);
		
		IntegrationImportedProducts importedProducts = new IntegrationImportedProducts();
		importedProducts.setTotalPages(response.getTotalPages() -1 );
		importedProducts.setAllShopsProducts(allShopsProducts);
		
		return importedProducts;
	}





	private List<ShopImportedProducts> getAllShopsProducts(ProductsResponse response, Long orgId) {
		List<ShopImportedProducts>  allShopsProducts= 
				ofNullable(response)
					.map(ProductsResponse::getProducts)
					.orElse(emptyList())
					.stream()
					.filter(Objects::nonNull)
					.map(product -> toListOfProductImportDTOWithShop(product, orgId))
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
	
	
	
	
	
	private List<ProductImportDTOWithShop> toListOfProductImportDTOWithShop(Product product, Long orgId) {
		return ofNullable(product)
				.map(Product::getStocks)
				.orElse(emptyList())
				.stream()
				.filter(this::isValidStock)
				.map(stock -> toProductImportDTOWithShop(product, stock, orgId))
				.collect(Collectors.toList());
	}





	private boolean isValidStock(Stocks stock) {
		return isNotBlankOrNull(stock.getStoreCode()) 
				&& stock.getValue() != null
				&& BigDecimal.ZERO.compareTo(stock.getValue()) < 0;
	} 
	
	
	
	
	private ProductImportDTOWithShop toProductImportDTOWithShop(Product product, Stocks stock, Long orgId) {
		ProductImportDTOWithShop importDto = new ProductImportDTOWithShop();
		String externalShopId = stock.getStoreCode();
		
		ProductImportDTO productImportDto = toProductImportDto(product, stock);
		
		importDto.setExternalShopId(externalShopId);
		importDto.setProductDto(productImportDto);
		
		return importDto;
	}





	private ProductImportDTO toProductImportDto(Product product, Stocks stock) {
		Integer stockQty =
				ofNullable(stock.getValue())
					.map(BigDecimal::intValue)
					.orElse(0);
		
		ProductImportDTO productImportDto = new ProductImportDTO();
		productImportDto.setBarcode(product.getSku());
		productImportDto.setBrand(product.getBrand());
		productImportDto.setTags( setOf(product.getCategory()));
		productImportDto.setDescription(product.getItemDescription());
		productImportDto.setExternalId(product.getAxId());
		productImportDto.setName(product.getName());
		productImportDto.setPrice(product.getOriginalPrice());		
		productImportDto.setQuantity(stockQty);
		
		return productImportDto;
	}
	

}



@Data
@AllArgsConstructor
class ProductResponseWithOrgId{
	private ProductsResponse productResponse;
	private Long orgId;
}
