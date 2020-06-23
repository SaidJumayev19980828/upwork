package com.nasnav.enumerations;

import lombok.Getter;

public enum ShippingStatus {


    DRAFT(0);

    @Getter
    private Integer value;

    ShippingStatus(Integer value) {
        this.value = value;
    }
}
