package com.nasnav.enumerations;

import lombok.Getter;

public enum ReturnRequestStatus {

    DRAFT(0),
    RECEIVED(1),
    REJECTED(2);

    @Getter
    private Integer value;

    ReturnRequestStatus(Integer value) {
        this.value = value;
    }
}
