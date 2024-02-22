package com.nasnav.enumerations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum ScrapingTypes {
   FILE_BASED ("file-based"),
    URL_BASED("url-based");

    @Getter
    private final String value;

    ScrapingTypes(String value) {
        this.value = value;
    }
}
