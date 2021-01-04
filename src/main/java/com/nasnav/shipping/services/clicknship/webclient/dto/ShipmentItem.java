package com.nasnav.shipping.services.clicknship.webclient.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ShipmentItem {
    public String itemName;
    public BigDecimal itemUnitCost;
    public Integer itemQuantity;
    public String itemColour;
    public String itemSize;
}
