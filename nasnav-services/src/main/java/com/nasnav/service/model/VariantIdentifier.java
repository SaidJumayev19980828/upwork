package com.nasnav.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VariantIdentifier {

    private String variantId;
    private String externalId;
    private String barcode;

    public VariantIdentifier(String barcode) {
        this.barcode = barcode;
    }

}
