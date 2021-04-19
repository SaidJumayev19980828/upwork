package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.nasnav.service.ProductImageService.PRODUCT_IMAGE;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Schema(name = "Images import meta data")
public class IntegrationImageImportDTO {
	private Integer pageNum;
	private Integer pageCount;
	private Integer type;
	private Integer priority;
	private Boolean ignoreErrors;
	private Boolean deleteOldImages;
	
	public IntegrationImageImportDTO() {
		this.priority= 0;
		this.type = PRODUCT_IMAGE;
		this.pageNum = 1;
		this.pageCount = 1000;
		this.deleteOldImages = false;
		this.ignoreErrors = false;
	}
}
