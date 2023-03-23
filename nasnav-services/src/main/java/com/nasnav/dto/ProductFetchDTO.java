package com.nasnav.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductFetchDTO {
    private Long productId;
    private Long shopId;
    private boolean checkVariants;
    private boolean includeOutOfStock;
    private Boolean onlyYeshteryProducts;

    public ProductFetchDTO(Long productId) {
        this.productId = productId;
        this.shopId = null;
        this.checkVariants = false;
        this.includeOutOfStock = false;
        this.onlyYeshteryProducts = false;
    }
}
