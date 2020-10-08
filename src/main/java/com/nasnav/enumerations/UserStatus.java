package com.nasnav.enumerations;

import lombok.Getter;

public enum UserStatus {

    NOT_ACTIVATED(200),
    ACTIVATED(201),
    ACCOUNT_SUSPENDED(202),
    INVALID_STATUS(0);

    @Getter
    private Integer value;

    UserStatus(Integer value) {
        this.value = value;
    }

    public static UserStatus getUserStatus(Integer value) {
        for(UserStatus status : UserStatus.values()) {
            if(status.value.equals(value))
                return status;
        }
        return null;
    }
}
