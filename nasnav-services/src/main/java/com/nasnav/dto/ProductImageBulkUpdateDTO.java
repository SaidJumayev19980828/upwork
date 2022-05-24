package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Schema(name = "Images import meta data")
public class ProductImageBulkUpdateDTO{
	private Integer type;
	private Integer priority;
	private boolean ignoreErrors;
	private boolean deleteOldImages;
	private Long featureId;
	private boolean crop;
	
	public ProductImageBulkUpdateDTO() {
		ignoreErrors = false;
	}

	public ProductImageBulkUpdateDTO(boolean ignoreErrors) {
		this.ignoreErrors = ignoreErrors;
	}
}
