package com.nasnav.service;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.response.PromotionDTO;
import lombok.*;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductsPromotionsDTO {

    private Long productId;
    private PromotionDTO promotionDTO;

    public ProductsPromotionsDTO(Long productId, PromotionDTO promotionDTO) {
        this.productId = productId;
        this.promotionDTO = promotionDTO;
    }
}