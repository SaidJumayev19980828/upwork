package com.nasnav.enumerations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CompensationActions {
    LIKE("Like"),
    SHARE("Share"),
    JOIN_EVENT("Join Event");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
