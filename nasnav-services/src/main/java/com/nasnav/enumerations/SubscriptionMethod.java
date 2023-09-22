package com.nasnav.enumerations;

import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

import static com.nasnav.exceptions.ErrorCodes.COINS$PARAM$0004;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public enum SubscriptionMethod {

    WERT("wert"),
    STRIPE("stripe");

    @Getter
    private final String value;

    SubscriptionMethod(String value) {
        this.value = value;
    }

}
