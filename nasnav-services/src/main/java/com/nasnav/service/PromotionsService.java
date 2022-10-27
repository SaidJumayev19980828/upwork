package com.nasnav.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.nasnav.dto.AppliedPromotionsResponse;
import com.nasnav.dto.PromoItemDto;
import com.nasnav.dto.PromotionSearchParamDTO;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.dto.response.PromotionResponse;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.persistence.PromotionsEntity;
import com.nasnav.persistence.UserEntity;

public interface PromotionsService {
	PromotionResponse getPromotions(PromotionSearchParamDTO searchParams);

	Long updatePromotion(PromotionDTO promotion);

	AppliedPromotionsResponse calcPromoDiscountForCart(String promoCode);
	AppliedPromotionsResponse calcPromoDiscountForCart(String promoCode, Cart cart);

	void setPromotionAsUsed(PromotionsEntity promotion, UserEntity user);

	/**
	 * set a used promo as un-used again
	 * */
	void redeemUsedPromotion(PromotionsEntity promotion, UserEntity user);

    void removePromotion(Long promotionId);

	AppliedPromotionsResponse calculateAllApplicablePromos(List<PromoItemDto> items, BigDecimal totalCartValue, String promoCode, Long orgId);

	BigDecimal calculateShippingPromoDiscount(BigDecimal totalShippingValue, BigDecimal totalCartValue);
	List<ProductsPromotionsDTO> getPromotionsListFromProductsList(Set<Long> ids);
}
