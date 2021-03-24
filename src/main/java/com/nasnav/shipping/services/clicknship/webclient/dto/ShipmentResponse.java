package com.nasnav.shipping.services.clicknship.webclient.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ShipmentResponse {
    public String transStatus;
    public String transStatusDetails;
    public String orderNo;
    public String waybillNumber;
    public String deliveryFee;
    public String vatAmount;
    public String totalAmount;
}
