package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@Data
public class ShopFloorsRequestDTO {
    private Long id;
    private Integer number;
    private String name;
    @JsonProperty("shop_sections")
    private List<ShopSectionsRequestDTO> shopSections;
}
