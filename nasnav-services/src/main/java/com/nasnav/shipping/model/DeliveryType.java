package com.nasnav.shipping.model;

import lombok.Getter;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public enum DeliveryType {
    NORMAL_DELIVERY("Normal Delivery"), SAME_DAY_DELIVERY("Same Day Delivery");

    @Getter
    private String value;

    DeliveryType(String value) {
        this.value = value;
    }

    public static List<String> getDeliveryTypes() {
        return stream(values())
                .map(DeliveryType::getValue)
                .collect(toList());
    }
}
