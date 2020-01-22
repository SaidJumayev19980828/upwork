package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class IntegrationProductImportDTO {
	@JsonProperty("dryrun")
	private boolean dryrun;	
	
	private boolean updateProduct;	
	private boolean updateStocks;
	private Integer currency;	
	private String encoding;
	private Integer pageNum;
	private Integer pageCount;
	
	
	
	public IntegrationProductImportDTO() {
		this.dryrun = true;
		this.updateProduct = false;
		this.updateStocks = false;
		currency = 1;
		encoding = "UTF-8";
		pageNum = 1;
		pageCount = 1000;
	}
}
