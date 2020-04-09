package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopScenesRequestDTO {
    private Long id;
    private String name;
    @JsonProperty("image_url")
    private String imageUrl;
}
