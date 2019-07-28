package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.ProductFeaturesEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VariantFeatureDTO extends BaseRepresentationObject {
	private String name;
	private String label;
	
	
	public VariantFeatureDTO(ProductFeaturesEntity entity) {
		this.name = entity.getName();
		this.label = entity.getPname();
	}
}
