package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String operation;
    @JsonProperty("parent_id")
    private Long parentId;
    private String logo;
}
