package com.nasnav.service;

import java.math.BigDecimal;
import java.util.List;

import com.nasnav.dto.PromoItemDto;
import com.nasnav.dto.PromotionSearchParamDTO;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.dto.response.PromotionResponse;
import com.nasnav.persistence.PromotionsEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.dto.query.result.CartItemData;

public interface PromotionsService {
	PromotionResponse getPromotions(PromotionSearchParamDTO searchParams);

	Long updatePromotion(PromotionDTO promotion);

	void setPromotionAsUsed(PromotionsEntity promotion, UserEntity user);

	/**
	 * set a used promo as un-used again
	 * */
	void redeemUsedPromotion(PromotionsEntity promotion, UserEntity user);

    void removePromotion(Long promotionId);

    BigDecimal calculateAllApplicablePromos(List<PromoItemDto> items, BigDecimal totalCartValue, String promoCode);

	BigDecimal calculateShippingPromoDiscount(BigDecimal totalShippingValue, BigDecimal totalCartValue);
}
