package com.nasnav.persistence.dto.query.result.products.export;

import lombok.Data;

@Data
public class VariantExtraAtrribute {
	private Long variantId;
	private Long id;
	private String name;
	private String value;
	
	
	
	public VariantExtraAtrribute(Long variantId, Long id, String name, String value) {
		this.variantId = variantId;
		this.id = id;
		this.name = name;
		this.value = value;
	}
}
