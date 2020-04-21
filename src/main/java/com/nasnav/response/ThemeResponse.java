package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThemeResponse {

    private Boolean success;

    @JsonProperty("id")
    private Integer id;

    public ThemeResponse(Integer id) {
        success = true;
        this.id = id;
    }
}
