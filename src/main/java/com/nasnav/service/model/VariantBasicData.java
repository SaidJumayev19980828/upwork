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
}
