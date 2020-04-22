package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ThemeResponse {

    @JsonProperty("theme_id")
    private Integer themeId;

    @JsonCreator
    public ThemeResponse(Integer themeId) {
        this.themeId = themeId;
    }
}
