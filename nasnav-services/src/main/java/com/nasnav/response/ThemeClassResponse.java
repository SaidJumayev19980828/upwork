package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

@Data
public class ThemeClassResponse {

    private Integer id;

    @JsonCreator
    public ThemeClassResponse(Integer id) {
        this.id = id;
    }
}
