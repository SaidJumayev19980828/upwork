package com.nasnav.enumerations;

import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

public enum VideoChatOrgState {
    DISABLED(0), ENABLED(1);

    VideoChatOrgState(Integer value){this.value = value;}

    @Getter
    private Integer value;

    public static Optional<VideoChatOrgState> getVideoChatState(Integer value) {
        return stream(values())
                .filter(n -> Objects.equals(n.getValue(), value))
                .findFirst();
    }
}
