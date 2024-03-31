package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.response.ThreeDModelResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductDetailsDTO extends ProductRepresentationObject {
	
	private String description;
	private String sku;
	private String productCode;
	private Long organizationId;
	private List<VariantDTO> variants;
	private List<VariantFeatureDTO> variantFeatures;
	private List<ProductRepresentationObject> bundleItems;
	private List<SeoKeywordsDTO> keywords;
	private ThreeDModelResponse threeDModel;

	public ProductDetailsDTO() {
		variants = new ArrayList<>();
		variantFeatures = new ArrayList<>();
	}
}
