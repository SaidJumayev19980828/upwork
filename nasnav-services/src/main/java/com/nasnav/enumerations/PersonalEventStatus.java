package com.nasnav.enumerations;

import lombok.Getter;

@Getter
public enum PersonalEventStatus {
    NOT_STARTED(0), STARTED(1), SUSPENDED(2) ,ENDED(3);

    private final Integer value ;
    PersonalEventStatus(Integer value) {
        this.value = value;
    }
}
