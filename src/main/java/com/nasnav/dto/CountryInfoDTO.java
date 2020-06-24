package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CountryInfoDTO {
    private String name;
    private String type;
    @JsonProperty("parent_id")
    private Long parentId;
}
