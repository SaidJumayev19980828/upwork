package com.nasnav.integration.model;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductImportDTOWithShop{
	private Long orgId;
	private String externalShopId;
	private ProductImportDTO productDto;
}