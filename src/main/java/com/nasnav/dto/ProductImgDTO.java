package com.nasnav.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.ProductImagesEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductImgDTO extends BaseRepresentationObject {
	private String url;
	private Integer priority;
	
	public ProductImgDTO(ProductImagesEntity entity) {
		this.url = entity.getUri();
		this.priority = entity.getPriority();
	}
}
