package com.nasnav.commons.model.handler;


import com.nasnav.dto.ProductUpdateDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;


//TODO Check Duplication DataImportServiceImpl

@Data
public class ProductData {

    private ProductUpdateDTO productDto;

    private List<VariantDTOWithExternalIdAndStock> variants;

    private String originalRowData;

    private Set<String> tagsNames;

    public ProductData() {

        variants = new ArrayList<>();
        productDto = new ProductUpdateDTO();
        originalRowData = "[]";
        tagsNames = emptySet();
    }

    public boolean isExisting() {

        return ofNullable(productDto)
                .map(ProductUpdateDTO::getId)
                .isPresent();
    }

}
