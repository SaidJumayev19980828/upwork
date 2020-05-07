package com.nasnav.dto.response.navbox;

import com.nasnav.dto.Prices;
import lombok.Data;

import java.util.List;

@Data
public class ThreeSixtyProductsDTO {

    private Long id;
    private String name;
    private String description;
    private List<String> images;
    private Prices prices;

    public ThreeSixtyProductsDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
