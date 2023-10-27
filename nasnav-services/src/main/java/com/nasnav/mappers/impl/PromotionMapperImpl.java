package com.nasnav.mappers.impl;

import com.nasnav.dto.OrderAssociatedPromotions;
import com.nasnav.dto.ProductPromotionDto;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.enumerations.PromotionType;
import com.nasnav.mappers.PromotionMapper;
import com.nasnav.persistence.PromotionsEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class PromotionMapperImpl implements PromotionMapper {
    @Override
    public OrderAssociatedPromotions toDto(PromotionsEntity entity) {
        if ( entity == null ) {
            return null;
        }

        OrderAssociatedPromotions orderAssociatedPromotions = new OrderAssociatedPromotions();

        orderAssociatedPromotions.setId( entity.getId() );
        orderAssociatedPromotions.setIdentifier( entity.getIdentifier() );
        orderAssociatedPromotions.setName( entity.getName() );
        orderAssociatedPromotions.setCode( entity.getCode() );

        return orderAssociatedPromotions;
    }

    @Override
    public List<ProductPromotionDto> toProductPromotionDto(List<PromotionDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<ProductPromotionDto> list = new ArrayList<ProductPromotionDto>( dtos.size() );
        for ( PromotionDTO promo : dtos ) {
            list.add( promotionsEntityToProductPromotionDto(promo));
        }

        return list;
    }

    protected ProductPromotionDto promotionsEntityToProductPromotionDto(PromotionDTO promotionDTO) {
        if ( promotionDTO == null ) {
            return null;
        }

        ProductPromotionDto productPromotionDto = new ProductPromotionDto();
        productPromotionDto.setId(promotionDTO.getId());
        productPromotionDto.setIdentifier( promotionDTO.getIdentifier() );
        productPromotionDto.setName( promotionDTO.getName() );
        productPromotionDto.setOrganizationId(promotionDTO.getOrganizationId());
        productPromotionDto.setDateStart( promotionDTO.getStartDate());
        productPromotionDto.setDateEnd( promotionDTO.getEndDate());
        productPromotionDto.setType(mapPromotionType(promotionDTO.getTypeId()));
        productPromotionDto.setCode(promotionDTO.getCode());
        productPromotionDto.setConstrains(promotionDTO.getConstrains());

        return productPromotionDto;
    }

    private String mapPromotionType(int promotionTypeValue) {
        for (PromotionType promotionType : PromotionType.values()) {
            if (Objects.equals(promotionType.getValue(), promotionTypeValue)) {
                return promotionType.name();
            }
        }
        return null;
    }

}
