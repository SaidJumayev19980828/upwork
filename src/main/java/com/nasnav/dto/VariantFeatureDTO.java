package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.persistence.ProductFeaturesEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

import static java.util.Collections.emptyMap;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VariantFeatureDTO extends BaseRepresentationObject {
	private String name;
	private String label;
	private String type;
	private Map<String, ?> extraData;

	public VariantFeatureDTO(){
		extraData = emptyMap();
	}
}
