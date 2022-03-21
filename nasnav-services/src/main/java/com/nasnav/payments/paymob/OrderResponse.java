package com.nasnav.payments.paymob;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrderResponse {
    private Long id;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("delivery_needed")
    private Boolean deliveryNeeded;
    @JsonProperty("collector")
    private String collector;
    @JsonProperty("amount_cents")
    private BigDecimal amountCents;
    @JsonProperty("shipping_data")
    private ShippingData shippingData;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("is_payment_locked")
    private Boolean isPaymentLocked;
    @JsonProperty("merchant_order_id")
    private String merchantOrderId;
    @JsonProperty("wallet_notification")
    private String walletNotification;
    @JsonProperty("paid_amount_cents")
    private BigDecimal paidAmountCents;
    @JsonProperty("items")
    private List<Items> items;

    @JsonProperty("order_url")
    private String orderUrl;
    @JsonProperty("url")
    private String url;
    @JsonProperty("payment_method")
    private String paymentMethod;
}
