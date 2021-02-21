package com.nasnav.enumerations;

import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

public enum ExtraAttributeType {
    STRING("String"), INVISIBLE("Invisible");

    @Getter
    private final String value;

    ExtraAttributeType(String value) {
        this.value = value;
    }

    public static Optional<ExtraAttributeType> getExtraAttributeType(String value) {
        return stream(values())
                .filter(s -> Objects.equals(s.getValue(), value))
                .findFirst();
    }
}
