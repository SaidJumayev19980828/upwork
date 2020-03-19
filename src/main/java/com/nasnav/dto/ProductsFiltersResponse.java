package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.dto.response.navbox.VariantFeatureDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductsFiltersResponse {

    private Prices prices;
    private List<Organization_BrandRepresentationObject> brands;
    @JsonProperty("variant_features")
    private List<VariantFeatureDTO.VariantFeatureDTOList> variantFeatures;
}
