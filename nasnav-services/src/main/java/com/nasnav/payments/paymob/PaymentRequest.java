package com.nasnav.payments.paymob;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.PaymobSourceEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PaymentRequest {
    String auth_token;
    BigDecimal amount_cents;
    Long expiration;
    String order_id;
    String currency = "EGP";
    long integration_id;
    boolean lock_order_when_paid = false;

    public static PaymentRequest fromOrderResponse(OrderResponse orderResponse, PaymobSourceEntity source) {
        PaymentRequest payReq = new PaymentRequest();
        payReq.setAmount_cents(orderResponse.getAmountCents());
        payReq.setCurrency(orderResponse.getCurrency());
        payReq.setExpiration(6000L);
        payReq.setOrder_id(orderResponse.getId().toString());
        payReq.setIntegration_id(Long.valueOf(source.getValue()));
        return payReq;
    }
}
