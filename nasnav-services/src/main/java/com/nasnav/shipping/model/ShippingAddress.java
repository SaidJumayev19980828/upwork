package com.nasnav.shipping.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShippingAddress{
    private String name;
    private String postalCode;
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
    private Long subArea;
}
