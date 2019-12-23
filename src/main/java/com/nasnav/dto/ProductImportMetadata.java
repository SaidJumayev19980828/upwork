package com.nasnav.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class ProductImportMetadata {

    private boolean dryrun;
    private boolean updateProduct;
    private boolean updateStocks;

    private Long shopId;
    private Integer currency;
    private String encoding;
}
