package com.nasnav.enumerations;

import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

import static com.nasnav.exceptions.ErrorCodes.PROMO$PARAM$0012;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public enum PromotionType {
    PROMO_CODE(0),
    SHIPPING(1),
    BUY_X_GET_Y_FROM_BRAND(2),
    TOTAL_CART_ITEMS_VALUE(3),
    TOTAL_CART_ITEMS_QUANTITY(4),
    BUY_X_GET_Y_FROM_TAG(5),
    BUY_X_GET_Y_FROM_PRODUCT(6);

    @Getter
    private Integer value;

    PromotionType(Integer value) {
        this.value = value;
    }

    public static PromotionType getPromotionType(Integer value) {
        return Arrays.stream(PromotionType.values())
                .filter(p -> Objects.equals(p.value, value))
                .findFirst()
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0012, value));
    }
}
