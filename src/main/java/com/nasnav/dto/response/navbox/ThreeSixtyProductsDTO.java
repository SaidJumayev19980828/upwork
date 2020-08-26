package com.nasnav.dto.response.navbox;

import com.nasnav.dto.Prices;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ThreeSixtyProductsDTO {

    private Long id;
    private String name;
    private String description;
    private Set<String> images;
    private Prices prices;
    private Integer productType;

    public ThreeSixtyProductsDTO(Long id, String name, String description, Integer productType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.productType = productType;
        images = new HashSet<>();
    }
}
