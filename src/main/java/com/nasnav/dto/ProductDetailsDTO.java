package com.nasnav.dto;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductDetailsDTO extends ProductRepresentationObject {
	
	private String description;
	private Integer productType;
	private List<ProductImageDTO> images;
	private List<VariantDTO> variants;
	private List<VariantFeatureDTO> variantFeatures;
	private List<ProductRepresentationObject> bundleItems;
	
	@JsonIgnore
	public  String getImageUrl() {
		return null;
	}
}
