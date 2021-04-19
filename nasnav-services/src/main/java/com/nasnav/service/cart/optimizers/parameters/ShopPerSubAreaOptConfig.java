package com.nasnav.service.cart.optimizers.parameters;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ShopPerSubAreaOptConfig {
    private Map<String,Long> subAreaShopMapping;
    private Long defaultShop;
}
