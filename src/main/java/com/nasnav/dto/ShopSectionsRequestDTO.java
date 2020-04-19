package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
public class ShopSectionsRequestDTO {
    private Long id;
    private String name;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("scenes")
    private List<ShopScenesRequestDTO> shopScenes;
}
