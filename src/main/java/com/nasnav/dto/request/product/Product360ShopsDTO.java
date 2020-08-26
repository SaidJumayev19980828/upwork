package com.nasnav.dto.request.product;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Set;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Product360ShopsDTO {
    private Boolean include;
    private Set<Long> productIds;
    private Set<Long> shopIds;
}
