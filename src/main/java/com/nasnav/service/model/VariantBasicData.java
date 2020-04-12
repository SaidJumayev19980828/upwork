package com.nasnav.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VariantBasicData {
	private Long variantId;
	private Long productId;
	private Long organizationId;
	private String barcode;
	
	public VariantBasicData(Long variantId, Long productId, Long organizationId, String barcode) {
		this.variantId = variantId;
		this.productId = productId;
		this.organizationId = organizationId;
		this.barcode = barcode;
	}
}
