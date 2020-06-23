package com.nasnav.dto.request.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class CartCheckoutDTO {
    @JsonProperty("customer_address")
    private Long addressId;
    @JsonProperty("shipping_service_id")
    private String serviceId;
    @JsonProperty("additional_data")
    private Map<String, ?> additionalData;
}
