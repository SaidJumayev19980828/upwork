package com.nasnav.shipping.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShipmentValidation {
    private Boolean isValid;
    private String message;
    
    public ShipmentValidation(Boolean isValid) {
    	this.isValid = isValid;
    }
}
