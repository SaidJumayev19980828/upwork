package com.nasnav.shipping.services.clicknship.webclient.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DeliveryFeeRequest {
    private String origin;
    private String destination;
    private String weight;
    private String pickupType;
    private String onforwardingTownID;
}
