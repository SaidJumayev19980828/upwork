package com.nasnav.dto.response.navbox;

import lombok.Data;

import java.util.List;

@Data
public class VariantFeatureDTO {
    private Long id;
    private String name;
    private List<String> values;


    public static class VariantFeatureDTOList extends VariantFeatureDTO {
        private String values;
    }
}
