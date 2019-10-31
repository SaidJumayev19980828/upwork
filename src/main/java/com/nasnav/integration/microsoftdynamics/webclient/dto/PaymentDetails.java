package com.nasnav.integration.microsoftdynamics.webclient.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDetails {
    private String salesId;
    private BigDecimal amount;
    private String paymentMethod;
}
