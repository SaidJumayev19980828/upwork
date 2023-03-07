package com.nasnav.persistence.dto.query.result.products;

import lombok.Data;

@Data
public class ProductAddonBasicData {

	private Long productId;
	private Long addonId;
	private String name;
	public ProductAddonBasicData(Long productId, Long addonId, String name) {
		super();
		this.productId = productId;
		this.addonId = addonId;
		this.name = name;
	}
	
	
	
}
