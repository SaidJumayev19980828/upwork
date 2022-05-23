package com.nasnav.enumerations;

import lombok.Getter;

public enum PaymobName {
    Online_Card("1627922"),
    Cash_Collection("1974407"),
    Accept_Kiosk("1739266"),
    Mobile_Wallet("1928804");

    @Getter
    private final String value;

    PaymobName(String value){
        this.value = value;
    }
}