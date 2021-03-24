package com.nasnav.shipping.services.clicknship.webclient.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ShipmentRequest {
    private String orderNo;
    private String description;
    private BigDecimal weight;
    private String senderName;
    private String senderCity;
    private String senderTownID;
    private String senderAddress;
    private String senderPhone;
    private String senderEmail;
    private String recipientName;
    private String recipientCity;
    private String recipientTownID;
    private String recipientAddress;
    private String recipientPhone;
    private String recipientEmail;
    private String paymentType;
    private String deliveryType;
    private String pickupType;
    private List<ShipmentItem> shipmentItems;
}
