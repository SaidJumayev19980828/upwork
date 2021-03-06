package com.nasnav.enumerations;

import lombok.Getter;

public enum PromotionType {
    PROMO_CODE(0),
    SHIPPING(1),
    BUY_X_GET_Y(2),
    TOTAL_CART_ITEMS_VALUE(3),
    TOTAL_CART_ITEMS_QUANTITY(4);

    @Getter
    private Integer value;

    PromotionType(Integer value) {
        this.value = value;
    }
}
