package com.nasnav.service.cart.optimizers.parameters;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PickupParameters {
    @JsonProperty("SHOP_ID")
    private Long shopId;
}
