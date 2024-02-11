package com.nasnav.enumerations;

import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

import static com.nasnav.exceptions.ErrorCodes.LOY$PARAM$0004;
import static com.nasnav.exceptions.ErrorCodes.REF$PARAM$0001;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Getter
@AllArgsConstructor
public enum ReferralCodeType {
    FIRST_USED_VALUE(1),
    SHARE_REVENUE_PERCENTAGE(2),
    ORDER_DISCOUNT_PERCENTAGE(3);

    private int value;

    public static ReferralCodeType getType(Integer value) {
        return Arrays.stream(ReferralCodeType.values())
                .filter(p -> Objects.equals(p.value, value))
                .findFirst()
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0001, value));
    }
}
