package com.nasnav.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.request.ProductPositionDTO;
import lombok.Data;

import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductsPositionDTO {

    private Long shopId;
    private Map<Long, ProductPositionDTO> productsData;
    Map<Long, ProductPositionDTO> collectionsData;

}
