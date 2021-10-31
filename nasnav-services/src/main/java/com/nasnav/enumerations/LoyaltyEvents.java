package com.nasnav.enumerations;

import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

import static com.nasnav.exceptions.ErrorCodes.COINS$PARAM$0004;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public enum LoyaltyEvents {

    BIRTH_DAY(0),
    SIGN_UP(1),
    GLOBAL_DATE_RAMADAN(2),
    GLOBAL_DATE_FESTIVAL(3),
    NEW_TIER(4),
    CUSTOMER_INVITE(5),
    NEW_FAMILY_MEMBER(6),
    NEW_FAMILY_PURCHASE(7);

    @Getter
    private final Integer value;

    LoyaltyEvents(Integer value) {
        this.value = value;
    }

    public static LoyaltyEvents getLoyaltyEvents(Integer value) {
        return Arrays.stream(LoyaltyEvents.values())
                .filter(p -> Objects.equals(p.value, value))
                .findFirst()
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, COINS$PARAM$0004, value));
    }
}
