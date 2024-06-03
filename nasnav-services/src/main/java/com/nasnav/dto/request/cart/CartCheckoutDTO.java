package com.nasnav.dto.request.cart;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Data
public class CartCheckoutDTO {
    @JsonProperty("customer_address")
    private Long addressId;
    @JsonProperty("shipping_service_id")
    private String serviceId;
    @JsonProperty("additional_data")
    private Map<String, String> additionalData;
    @JsonProperty("promo_code")
    private String promoCode;
    private String notes;
    private Set<Long> points;

    private BigDecimal requestedPoints;
    private Long customerId;
    private String referralCode;

    private boolean payFromReferralBalance = false;
    private String otp;

    @JsonIgnore
    private Long createdByEmployeeId;
    @JsonIgnore
    private boolean isTwoStepVerified;
    private Set<Long> selectedStockIds;

}
