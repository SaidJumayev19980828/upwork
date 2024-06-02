package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductListImportDTO {
	private boolean dryrun;	
	private boolean updateProduct;	
	private boolean updateStocks;
	private boolean deleteOldProducts;
	private boolean resetTags;
    private boolean insertNewProducts;
	
	private List<Long> shopIds;
	private Integer currency;	
	private String encoding;	
	
	public ProductListImportDTO(){
		this.insertNewProducts = true;
	}
}
