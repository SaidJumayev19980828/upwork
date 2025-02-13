package com.nasnav.enumerations;

import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

public enum VideoChatStatus {
    FAILED(0), NEW(1), STARTED(2), ENDED_BY_CUSTOMER(3), ENDED_BY_EMPLOYEE(4), ENDED_BY_CALLBACK(5);

    VideoChatStatus(Integer value){this.value = value;}

    @Getter
    private Integer value;

    public static Optional<VideoChatStatus> getVideoChatState(Integer value) {
        return stream(values())
                .filter(n -> Objects.equals(n.getValue(), value))
                .findFirst();
    }
}
