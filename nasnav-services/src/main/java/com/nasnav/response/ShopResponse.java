package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShopResponse {

    @JsonProperty(value = "shop_id")
    private Long storeId;
}
