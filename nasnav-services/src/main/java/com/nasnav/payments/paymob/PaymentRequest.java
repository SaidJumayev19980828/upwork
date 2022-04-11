package com.nasnav.payments.paymob;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PaymentRequest {
    String auth_token;
    BigDecimal amount_cents;
    long expiration;
    String order_id;
    String currency = "EGP";
    long integration_id;
    boolean lock_order_when_paid = true;

    public static PaymentRequest fromOrderResponse(OrderResponse orderResponse) {
        PaymentRequest payReq = new PaymentRequest();
        payReq.setAmount_cents(orderResponse.getAmountCents());
        payReq.setCurrency(orderResponse.getCurrency());
        payReq.setExpiration(6000);
        payReq.setOrder_id(orderResponse.getId().toString());
        payReq.setIntegration_id(1627922); // TODO
        return payReq;
    }
}
