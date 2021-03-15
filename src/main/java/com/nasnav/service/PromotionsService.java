package com.nasnav.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.nasnav.dto.PromotionSearchParamDTO;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.dto.response.PromotionResponse;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.persistence.PromotionsEntity;
import com.nasnav.persistence.UserEntity;

public interface PromotionsService {
	PromotionResponse getPromotions(PromotionSearchParamDTO searchParams);

	Long updatePromotion(PromotionDTO promotion);

	BigDecimal calcPromoDiscountForCart(String promoCode);

	BigDecimal calcPromoDiscount(String promoCode, BigDecimal subTotal);

	void setPromotionAsUsed(PromotionsEntity promotion, UserEntity user);

	/**
	 * set a used promo as un-used again
	 * */
	void redeemUsedPromotion(PromotionsEntity promotion, UserEntity user);

    void removePromotion(Long promotionId);

	BigDecimal calculateBuyXGetYPromoDiscount( List<CartItem> items) throws IOException;
	BigDecimal calculateShippingPromoDiscount(BigDecimal totalShippingValue);
	BigDecimal calculateTotalCartDiscount();
}
