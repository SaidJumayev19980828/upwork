package com.nasnav.mappers.impl;

import com.nasnav.dto.OrderAssociatedPromotions;
import com.nasnav.dto.ProductPromotionDto;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.persistence.PromotionsEntity;

import java.util.List;

public interface promotionToDTO {

    OrderAssociatedPromotions toDto(PromotionsEntity entity);
    List<ProductPromotionDto> toProductPromotionDto(List<PromotionDTO> entity);
}