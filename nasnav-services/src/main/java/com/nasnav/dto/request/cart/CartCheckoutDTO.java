package com.nasnav.dto.request.cart;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.enumerations.PaymentMethods;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Data
public class CartCheckoutDTO {
    @JsonProperty("customer_address")
    private Long addressId;
    @JsonProperty("shipping_service_id")
    private String serviceId;  // this one used
    @JsonProperty("additional_data")
    private Map<String, String> additionalData;  // also this  shop_id
    @JsonProperty("promo_code")
    private String promoCode;
    private String notes;
    private Set<Long> points;

    @JsonProperty("payment_methods")
    @NotNull
    private PaymentMethods paymentMethods;


    private BigDecimal requestedPoints;
    private Long customerId;  // this one also
    private String referralCode;

    private boolean payFromReferralBalance = false;
    private String otp;  ///

    @JsonIgnore
    private Long createdByEmployeeId;
    @JsonIgnore
    private boolean isTwoStepVerified;
    private Set<Long> selectedStockIds;  ///

}
