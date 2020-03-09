package com.nasnav.dto;

import lombok.Data;

@Data
public class VariantWithNoImagesDTO {
	private Long variantId;
	private String barcode;
	private String externalId;
	private String productName;
	private Long productId;
}
