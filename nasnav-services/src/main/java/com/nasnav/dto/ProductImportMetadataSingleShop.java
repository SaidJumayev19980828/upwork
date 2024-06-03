package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class ProductImportMetadataSingleShop {
    private boolean dryrun;

    private boolean updateProduct;
    private boolean updateStocks;
    private boolean deleteOldProducts;
    private boolean resetTags;
    private boolean insertNewProducts;

    private Long shopId;
    private Integer currency;
    private String encoding;
}
