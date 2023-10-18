package com.nasnav.enumerations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatSettingType {
    PUBLISHED(1),
    UNPUBLISHED(2);
    @Getter
    private final int value;
}
