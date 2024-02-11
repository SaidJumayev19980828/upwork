package com.nasnav.enumerations;

import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

import static com.nasnav.exceptions.ErrorCodes.REF$PARAM$0004;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@AllArgsConstructor
@Getter
public enum ReferralCodeStatus {
    IN_ACTIVE(1), ACTIVE(2);

    private int value;

    public static ReferralCodeStatus getStatus(Integer value) {
        return Arrays.stream(ReferralCodeStatus.values())
                .filter(p -> Objects.equals(p.value, value))
                .findFirst()
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0004, value));
    }

}
