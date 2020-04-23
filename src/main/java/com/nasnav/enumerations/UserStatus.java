package com.nasnav.enumerations;

import lombok.Getter;

public enum UserStatus {

    NOT_ACTIVATED(200),
    ACTIVATED(201),
    ACCOUNT_SUSPENDED(202);

    @Getter
    private Integer value;

    UserStatus(Integer value) {
        this.value = value;
    }
}
