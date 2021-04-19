package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductDetailsDTO extends ProductRepresentationObject {
	
	private String description;
	private Integer productType;
	private String sku;
	private String productCode;
	private List<VariantDTO> variants;
	private List<VariantFeatureDTO> variantFeatures;
	private List<ProductRepresentationObject> bundleItems;

	public ProductDetailsDTO() {
		variants = new ArrayList<>();
		variantFeatures = new ArrayList<>();
	}
}
