package com.nasnav.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentValidation {
    private Boolean isValid;
    private String message;
    
    public ShipmentValidation(Boolean isValid) {
    	this.isValid = isValid;
    }
}
