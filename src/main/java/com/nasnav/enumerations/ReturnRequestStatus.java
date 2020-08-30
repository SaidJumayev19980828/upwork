package com.nasnav.enumerations;

import lombok.Getter;

public enum ReturnRequestStatus {

    RECEIVED(1);

    @Getter
    private Integer value;

    ReturnRequestStatus(Integer value) {
        this.value = value;
    }
}
