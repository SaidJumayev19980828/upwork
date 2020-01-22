package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDetails {
    @JsonProperty("SalesId")
    private String salesId;
    @JsonProperty("Amount")
    private BigDecimal amount;
    @JsonProperty("PaymentMethod")
    private String paymentMethod;
}
