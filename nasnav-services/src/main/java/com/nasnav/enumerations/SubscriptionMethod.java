package com.nasnav.enumerations;

import lombok.Getter;


public enum SubscriptionMethod {

    WERT("wert"),
    STRIPE("stripe");

    @Getter
    private final String value;

    SubscriptionMethod(String value) {
        this.value = value;
    }

}
