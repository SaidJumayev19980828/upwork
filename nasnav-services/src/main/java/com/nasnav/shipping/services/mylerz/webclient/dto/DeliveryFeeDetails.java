package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryFeeDetails {
    @JsonProperty("CODValue")
    private BigDecimal codValue;
    @JsonProperty("TransferValue")
    private BigDecimal transferValue;
    @JsonProperty("ShippingFees")
    private BigDecimal shippingFees;
    @JsonProperty("Charges")
    private BigDecimal charges;
    @JsonProperty("FulfillmentFees")
    private BigDecimal fulfillmentFees;
    @JsonProperty("NetTransferValue")
    private BigDecimal netTransferValue;
    @JsonProperty("VATPercetage")
    private BigDecimal vatPercentage;
    @JsonProperty("VAT")
    private BigDecimal vat;
    @JsonProperty("TotalTransfer")
    private BigDecimal totalTransfer;
}
