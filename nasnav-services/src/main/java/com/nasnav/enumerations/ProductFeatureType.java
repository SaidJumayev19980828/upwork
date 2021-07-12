package com.nasnav.enumerations;

import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

public enum ProductFeatureType {
    STRING(0), IMG_SWATCH(1), COLOR(2);

    @Getter
    private final Integer value;


    ProductFeatureType(Integer value){
        this.value = value;
    }


    public static Optional<ProductFeatureType> getProductFeatureType(Integer value) {
        return stream(values())
                .filter(n -> Objects.equals(n.getValue(), value))
                .findFirst();
    }
}
