package com.nasnav.enumerations;

import lombok.Getter;

public enum PaymobName {
    ONLINE_CARD("1627922"),
    CASH_COLLECTION("1974407"),
    ACCEPT_KIOSK("1739266"),
    MOBILE_WALLET("1928804");

    @Getter
    private final String value;

    PaymobName(String value){
        this.value = value;
    }
}