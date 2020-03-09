package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@ApiModel(value = "Images import meta data")
public class ProductImageBulkUpdateDTO{
	private Integer type;
	private Integer priority;
	private Boolean ignoreErrors;
	
	public ProductImageBulkUpdateDTO() {
		ignoreErrors = false;
	}
}
