package com.nasnav.mappers.impl;

import com.nasnav.dto.OrderAssociatedPromotions;
import com.nasnav.dto.ProductPromotionDto;
import com.nasnav.dto.PromosConstraints;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.enumerations.PromotionType;
import com.nasnav.persistence.PromotionsEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Component
public class PromotionToDTOImpl implements promotionToDTO {

  private static final   List<Integer> PROMOS_TYPE_IDS = List.of(PromotionType.BUY_X_GET_Y_FROM_BRAND.getValue()
            , PromotionType.BUY_X_GET_Y_FROM_TAG.getValue()
            , PromotionType.PROMO_CODE_FROM_PRODUCT.getValue());
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
        productPromotionDto.setAppliedPromotion(getActualPromotion(promotionDTO.getConstrains(), promotionDTO.getTypeId()));
        return productPromotionDto;
    }

    private String mapPromotionType(int promotionTypeValue) {
        for (PromotionType promotionType : PromotionType.values()) {
            if (Objects.equals(promotionType.getValue(), promotionTypeValue)) {
                return promotionType.name();
            }
        }
        return "";
    }

    private String getActualPromotion(PromosConstraints constraints, int promoTypeId) {
        for(int  typeId : PROMOS_TYPE_IDS){
            if (typeId == promoTypeId) {
                String promotion = mapPromotionType(promoTypeId);
                Integer quantityMin = constraints.getProductQuantityMin();
                Integer productToGive = constraints.getProductToGive();
                if (quantityMin == null || productToGive == null) {
                    return promotion;
                }
                if (promotion.equals(PromotionType.BUY_X_GET_Y_FROM_BRAND.name()) || promotion.equals(PromotionType.BUY_X_GET_Y_FROM_TAG.name())
                    || promotion.equals(PromotionType.BUY_X_GET_Y_FROM_PRODUCT.name())) {
                    return replaceSecondOccurrence(promotion, String.valueOf(productToGive))
                            .replace("X", String.valueOf(quantityMin));
                }
            }
        }
        return mapPromotionType(promoTypeId);
    }

    private String replaceSecondOccurrence(String promoType, String replacement) {
        int first = promoType.indexOf('Y');
        int second = promoType.indexOf('Y', first + 1);
       return promoType.substring(0, second) + replacement + promoType.substring(second+1);
    }
}
