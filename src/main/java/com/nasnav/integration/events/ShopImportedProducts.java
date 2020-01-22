package com.nasnav.integration.events;

import java.util.List;

import com.nasnav.commons.model.dataimport.ProductImportDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShopImportedProducts{
	private Long shopId;
	private String externalShopId;
	private List<ProductImportDTO> importedProducts;
	
	public ShopImportedProducts(String externalShopId, List<ProductImportDTO> importedProducts) {
		this.externalShopId = externalShopId;
		this.importedProducts = importedProducts;
	}
}