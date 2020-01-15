package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TagsDTO {

    private Long id;
    @JsonProperty(value = "category_id")
    private Long categoryId;
    private String name;
    private String alias;
    private String metadata;
    @JsonProperty(required = true)
    private String operation;

}
