package com.nasnav.dto;

import static com.nasnav.service.ProductImageService.PRODUCT_IMAGE;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@ApiModel(value = "Images import meta data")
public class IntegrationImageImportDTO {
	private Integer pageNum;
	private Integer pageCount;
	private Integer type;
	private Integer priority;
	
	public IntegrationImageImportDTO() {
		this.priority= 0;
		this.type = PRODUCT_IMAGE;
		this.pageNum = 1;
		this.pageCount = 1000;
	}
}
