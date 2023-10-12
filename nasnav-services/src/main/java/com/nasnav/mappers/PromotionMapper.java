package com.nasnav.mappers;

import com.nasnav.dto.OrderAssociatedPromotions;
import com.nasnav.persistence.PromotionsEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
   OrderAssociatedPromotions toDto(PromotionsEntity entity);

}
