package com.nasnav.dto;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.ProductEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductDetailsDTO extends ProductRepresentationObject {
	private String description;
	private Integer productType;
	private List<ProductImgDTO> images;
	private List<VariantDTO> variants;
	private List<VariantFeatureDTO> variantFeatures;
	private List<ProductRepresentationObject> bundleItems;
	
	public ProductDetailsDTO(ProductEntity product) {
		BeanUtils.copyProperties(product.getRepresentation(), this);		
	}
}
