package com.nasnav.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;


@Getter
public class ShopIdAndPriority {
    @JsonProperty("shop_id")
    private Long shopId;
    private Integer priority;
}
