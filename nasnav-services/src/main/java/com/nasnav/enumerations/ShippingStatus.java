package com.nasnav.enumerations;

import lombok.Getter;
import java.util.Arrays;

public enum ShippingStatus {


    DRAFT(0),
    REQUESTED(1),
    EN_ROUTE(10),
    PICKED_UP(20),
    DELIVERED(45),
    CANCELED(50),
    FAILED(55),
    RETURNED(60),
    ERROR(-1);

    @Getter
    private Integer value;

    ShippingStatus(Integer value) {
        this.value = value;
    }

    public static String getShippingStatusName(int value) {
        return Arrays.stream(ShippingStatus.values())
                     .filter(s -> s.value == value)
                     .findFirst()
                     .map(Enum::name)
                     .orElseGet(ERROR::name);
    }
}
