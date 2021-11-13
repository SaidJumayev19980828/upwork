package com.nasnav.enumerations;

import lombok.Getter;

import java.util.Arrays;

public enum LoyaltyPointStatus {
    ACTIVE(1), INACTIVE(0);


    @Getter
    private Integer value;

    LoyaltyPointStatus(Integer value){
        this.value = value;
    }

    public static LoyaltyPointStatus get(Integer value){
        return Arrays.stream(LoyaltyPointStatus.values())
                .filter(status -> status.getValue() == value )
                .findFirst()
                .orElse(null);
    }
}
