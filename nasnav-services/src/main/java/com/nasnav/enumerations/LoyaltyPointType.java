package com.nasnav.enumerations;

import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

import static com.nasnav.exceptions.ErrorCodes.COINS$PARAM$0004;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public enum LoyaltyPointType {

    REFERRAL(0),
    ORDER_ONLINE(2),
    PICKUP_FROM_SHOP(3),
    SHARE(3),
    REVIEW_PRODUCT(4),
    SPEND_IN_ORDER(110),
    SPEND_IN_SHARE(111);

    @Getter
    private final Integer value;

    LoyaltyPointType(Integer value) {
        this.value = value;
    }

    public static LoyaltyPointType getLoyaltyPointType(Integer value) {
        return Arrays.stream(LoyaltyPointType.values())
                .filter(p -> Objects.equals(p.value, value))
                .findFirst()
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, COINS$PARAM$0004, value));
    }
}
