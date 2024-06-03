package com.nasnav.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.InfluencerReferralConstraints;
import com.nasnav.dto.referral_code.InfluencerReferralDto;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.persistence.OrdersEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

public interface InfluencerReferralService {

    @Transactional
    InfluencerReferralDto register(InfluencerReferralDto influencerDTO);
    InfluencerReferralDto getWalletBalance(String userName, String password);

    void updateReferralSettings(String username, InfluencerReferralConstraints influencerReferralConstraints) throws JsonProcessingException;

    PaginatedResponse<InfluencerReferralDto> getAllInfluencerReferrals(Integer pageNo, Integer pageSize);

    BigDecimal calculateDiscount(String influencerReferralCode, Set<OrdersEntity> suborders);


    BigDecimal calculateDiscountForCart(String influencerReferralCode, Cart cart);

    void addInfluencerCashback(OrdersEntity ordersEntity);
}
