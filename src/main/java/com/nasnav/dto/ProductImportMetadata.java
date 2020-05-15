package com.nasnav.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ProductImportMetadata {
    private boolean dryrun;
	
    private boolean updateProduct;
    private boolean updateStocks;
    private boolean deleteOldProducts;
    private boolean resetTags;
    private boolean insertNewProducts;

    private Long shopId;
    private Integer currency;
    private String encoding;
    
    public ProductImportMetadata() {
    	this.insertNewProducts = true;
    }
}
