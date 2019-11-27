package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TagsDTO {

    private Long id;
    private String name;
    @JsonProperty(required = true)
    private String operation;
}
