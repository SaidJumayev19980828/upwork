package com.nasnav.mappers;

import com.nasnav.dto.OrderAssociatedPromotions;
import com.nasnav.dto.ProductPromotionDto;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.enumerations.PromotionType;
import com.nasnav.persistence.PromotionsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Objects;


public interface PromotionMapper {
   OrderAssociatedPromotions toDto(PromotionsEntity entity);
   List<ProductPromotionDto> toProductPromotionDto(List<PromotionDTO> entity);
}
