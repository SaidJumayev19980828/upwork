package com.nasnav.enumerations;

import lombok.Getter;

import java.util.Objects;

public enum ReturnRequestStatus {

    NEW(0),
    RECEIVED(1),
    REJECTED(2),
    CONFIRMED(3);

    @Getter
    private Integer value;

    ReturnRequestStatus(Integer value) {
        this.value = value;
    }

    public static ReturnRequestStatus findEnum(Integer statusValue) {
        for (ReturnRequestStatus status : ReturnRequestStatus.values()) {
            if ( Objects.equals(status.getValue() ,statusValue) ) {
                return status;
            }
        }
        return null;
    }
}
