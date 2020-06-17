package com.nasnav.shipping.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ShippingAddress{
    private String name;
    private Long postalCode;
    private Long country;
    private Long city;
    private String notes;
    private Long id;
    private String flatNumber;
    private String buildingNumber;
    private String addressLine1;
    private String addressLine2;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long area;
}
