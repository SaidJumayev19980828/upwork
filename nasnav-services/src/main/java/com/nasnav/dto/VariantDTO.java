package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VariantDTO extends BaseRepresentationObject {
	private Long id;
	private String barcode;
	private String name;
	private String description;
	private String sku;
	private String productCode;
	private List<StockDTO> stocks;
	private List<ProductImageDTO> images;

	//this will be inserted unwrapped in JSON by @JsonAnyGetter , we don't
	//want it to be inserted twice
	@JsonIgnore
	private Map<String,String> variantFeatures;
	
	private List<ExtraAttributeDTO> extraAttributes;

	// used to insert the variant features key-value pairs into the
	// generated JSON of VariantDTO
	@JsonAnyGetter
	public Map<String,String> getVariantFeaturesMap() {
	    return variantFeatures;
	}
}
