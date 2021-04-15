package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.Prices;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ThreeSixtyProductsDTO {

    private Long id;
    private String name;
    private String description;
    private Integer productType;
    private Set<String> images;
    private String imageUrl;
    private Prices prices;

    public ThreeSixtyProductsDTO(Long id, String name, String description, Integer productType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.productType = productType;
        images = new HashSet<>();
    }
}
