package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;

@Data
public class ShopSectionsRequestDTO {
    private Long id;
    private String name;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("scenes")
    private Set<ShopScenesRequestDTO> shopScenes;
}
