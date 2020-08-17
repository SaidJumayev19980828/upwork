package com.nasnav.enumerations;

import lombok.Getter;

public enum ReturnRequestStatus {

    RECEIVED(0);

    @Getter
    private Integer value;

    ReturnRequestStatus(Integer value) {
        this.value = value;
    }
}
