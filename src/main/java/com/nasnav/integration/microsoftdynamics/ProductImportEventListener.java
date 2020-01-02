package com.nasnav.integration.microsoftdynamics;

import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_EXTERNAL_SHOP_NOT_FOUND;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

import java.math.BigDecimal;

import static java.lang.String.format;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.IntegrationImportedProducts;
import com.nasnav.integration.events.ProductsImportEvent;
import com.nasnav.integration.events.ShopImportedProducts;
import com.nasnav.integration.events.data.ProductImportEventParam;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Product;
import com.nasnav.integration.microsoftdynamics.webclient.dto.ProductsResponse;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Stocks;

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
				.filter( res -> res.statusCode() == HttpStatus.OK)
				.doOnSuccess(this::throwExceptionIfNotOk)
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
					.orElse(Collections.emptyList())
					.stream()
					.filter(Objects::nonNull)
					.map(product -> toProductImportDTOWithShopList(product, orgId))
					.flatMap(List::stream)
					.collect( 
							groupingBy(ProductImportDTOWithShop::getShopId
									, mapping(ProductImportDTOWithShop::getProductDto, toList())))
					.entrySet()
					.stream()
					.map(shopProductMapping -> new ShopImportedProducts(shopProductMapping.getKey(), shopProductMapping.getValue()))
					.collect(toList());
		return allShopsProducts;
	}
	
	
	
	
	
	private List<ProductImportDTOWithShop> toProductImportDTOWithShopList(Product product, Long orgId) {
		return ofNullable(product)
				.map(Product::getStocks)
				.orElse(Collections.emptyList())
				.stream()
				.filter(stock -> Objects.equals(stock.getValue(), BigDecimal.ZERO))
				.map(stock -> toProductImportDTOWithShop(product, stock, orgId))
				.collect(Collectors.toList());
	} 
	
	
	
	
	private ProductImportDTOWithShop toProductImportDTOWithShop(Product product, Stocks stock, Long orgId) {
		ProductImportDTOWithShop importDto = new ProductImportDTOWithShop();
		String externalShopId = stock.getStoreCode();
		String shopIdStr = integrationService.getLocalMappedValue(orgId, MappingType.SHOP, externalShopId);
		Long shopId = -1L;
		
		ProductImportDTO productImportDto = toProductImportDto(product, stock);
		
		try {
			shopId = Long.valueOf(shopIdStr.toString());
		}catch(Throwable t) {
			throw new RuntimeBusinessException(
					format(ERR_EXTERNAL_SHOP_NOT_FOUND, externalShopId)
					, "INTEGRATION FAILURE"
					, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		importDto.setShopId(shopId);
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
		productImportDto.setCategory(product.getCategory());
		productImportDto.setDescription(product.getItemDescription());
		productImportDto.setExternalId(product.getAxId());
		productImportDto.setName(product.getItemDescription());
		productImportDto.setPrice(product.getOriginalPrice());		
		productImportDto.setQuantity(stockQty);
		
		return productImportDto;
	}
	

}


@Data
class ProductImportDTOWithShop{
	private Long shopId;
	private ProductImportDTO productDto;
}




@Data
@AllArgsConstructor
class ProductResponseWithOrgId{
	private ProductsResponse productResponse;
	private Long orgId;
}
