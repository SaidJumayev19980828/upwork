package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class ProductImageDTO extends BaseRepresentationObject {
    private Long productId;
    private Long variantId;
    private String barcode;
    private String imagePath;

    public ProductImageDTO(String imagePath, Long productId, Long variantId) {
        this.imagePath = imagePath;
        this.productId = productId;
        this.variantId = variantId;
    }
}
