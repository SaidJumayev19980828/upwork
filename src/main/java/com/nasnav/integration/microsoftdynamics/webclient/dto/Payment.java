package com.nasnav.integration.microsoftdynamics.webclient.dto;

import lombok.Data;

import java.util.List;

@Data
public class Payment {
    private String salesId;
    private List<PaymentDetails> paymentDet;
}
