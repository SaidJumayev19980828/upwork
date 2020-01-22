package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductImageUpdateIdentifier {

    private String variantId;
    private String externalId;
    private String barcode;

    public ProductImageUpdateIdentifier(String barcode) {
        this.barcode = barcode;
    }

}
