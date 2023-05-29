package com.nasnav.enumerations;

import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

import static com.nasnav.exceptions.ErrorCodes.COINS$PARAM$0004;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public enum Services {

    TECHNOLOGY_360(1),
    MET_AVERSE(2),
    CHAT_SERVICES(3),
    VIRTUAL_LANDS(4);

    @Getter
    private final Integer value;

    Services(Integer value) {
        this.value = value;
    }

    public static Services getServices(Integer value) {
        return Arrays.stream(Services.values())
                .filter(p -> Objects.equals(p.value, value))
                .findFirst()
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, COINS$PARAM$0004, value));
    }
}
