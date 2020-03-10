package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopThreeSixtyDTO {

    private Long id;
    @JsonProperty("scene_name")
    private String name;
}
