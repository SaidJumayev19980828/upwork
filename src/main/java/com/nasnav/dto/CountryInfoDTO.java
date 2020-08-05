package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CountryInfoDTO {
    private Long id;
    private String name;
    private String type;
    @JsonProperty("parent_id")
    private Long parentId;

    public CountryInfoDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public CountryInfoDTO(Long id, String name, Long parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }
}
