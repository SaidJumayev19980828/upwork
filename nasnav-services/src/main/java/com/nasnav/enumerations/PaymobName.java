package com.nasnav.enumerations;

import lombok.Getter;

public enum PaymobName {
    ONLINE_CARD("Online Card"),
    CASH_COLLECTION("Cash Collection"),
    ACCEPT_KIOSK("Accept Kiosk"),
    MOBILE_WALLET("Mobile Wallet");

    @Getter
    private final String value;

    PaymobName(String value){
        this.value = value;
    }
}