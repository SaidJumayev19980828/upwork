package com.nasnav.payments.paymob;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PaymobPaymentResponse {
//    private String token;
    private String redirectionUrl;
    private String id;
    private Boolean success;
    private String hmac;
    private String pending;
    private BigDecimal amount_cents;
    private Boolean is_auth;
    private Boolean is_capture;
    private Boolean is_standalone_payment;
    private Boolean is_voided;
    private Boolean is_refunded;
    private Boolean is_3d_secure;
    private Integer integration_id;

}
