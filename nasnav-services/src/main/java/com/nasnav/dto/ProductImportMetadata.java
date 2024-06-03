package com.nasnav.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper=false)
public class ProductImportMetadata {
    private boolean dryrun;
	
    private boolean updateProduct;
    private boolean updateStocks;
    private boolean deleteOldProducts;
    private boolean resetTags;
    private boolean insertNewProducts;

    private List<Long> shopIds;
    private Integer currency;
    private String encoding;
    
    public ProductImportMetadata() {
    	this.insertNewProducts = true;
    }

    public List<ProductImportMetadataSingleShop> createMetadataSingleShops() {
        if (shopIds == null || shopIds.isEmpty()) {
            return Collections.emptyList();
        }

        return shopIds.stream()
                .map(shopId -> new ProductImportMetadataSingleShop(dryrun, updateProduct, updateStocks, deleteOldProducts,
                        resetTags, insertNewProducts, shopId, currency, encoding))
                .collect(Collectors.toList());
    }
}
