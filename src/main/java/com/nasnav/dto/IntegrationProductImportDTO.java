package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class IntegrationProductImportDTO {
	private boolean dryrun;	
	private boolean updateProduct;	
	private boolean updateStocks;
	private Integer currency;	
	private String encoding;
}
