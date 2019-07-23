package com.nasnav.dto;

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
	private List<StockDTO> stocks;
	private List<ProductImgDTO> images;
	private Map<String,String> variantFeatures;
}
