package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ProductsFiltersResponse {

    private Prices prices;
    private List<Organization_BrandRepresentationObject> brands;
    private List<TagsRepresentationObject> tags;
    @JsonProperty("variant_features")
    private Map<String, List<String>> variantFeatures;
}
