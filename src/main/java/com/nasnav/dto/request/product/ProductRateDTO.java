package com.nasnav.dto.request.product;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductRateDTO {
    private Long variantId;
    private Integer rate;
    private String review;
    private Long orderId;
}
