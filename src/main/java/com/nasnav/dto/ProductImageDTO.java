package com.nasnav.dto;

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
}
