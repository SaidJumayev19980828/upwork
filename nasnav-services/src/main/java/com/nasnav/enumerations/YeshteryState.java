package com.nasnav.enumerations;

import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

public enum YeshteryState {
    DISABLED(0), ACTIVE(1);

    YeshteryState(Integer value){this.value = value;}

    @Getter
    private Integer value;

    public static Optional<YeshteryState> getYeshteryState(Integer value) {
        return stream(values())
                .filter(n -> Objects.equals(n.getValue(), value))
                .findFirst();
    }
    public Integer getValue() {
        return value;
    }
}
