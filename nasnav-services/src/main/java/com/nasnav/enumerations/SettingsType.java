package com.nasnav.enumerations;

import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.Getter;

import java.util.Objects;

import static com.nasnav.exceptions.ErrorCodes.ORG$SETTING$0002;
import static java.util.Arrays.stream;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


public enum SettingsType {

    PUBLIC(0),
    PRIVATE(1);

    @Getter
    private int value;

    SettingsType(int value) {
        this.value = value;
    }

    public static SettingsType getSettingsType(Integer value) {
        return stream(values())
                .filter(s -> Objects.equals(s.value, value))
                .findFirst()
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SETTING$0002, value));
    }
}
