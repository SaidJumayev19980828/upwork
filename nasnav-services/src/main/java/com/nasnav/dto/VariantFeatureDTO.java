package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
