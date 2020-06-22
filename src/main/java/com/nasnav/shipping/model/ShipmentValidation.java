package com.nasnav.shipping.model;

import lombok.Data;

@Data
public class ShipmentValidation {
    private Boolean isValid;
    private String message;
}
