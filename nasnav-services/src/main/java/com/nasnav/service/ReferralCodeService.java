package com.nasnav.service;

import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.dto.referral_code.ReferralStatsDto;
import com.nasnav.dto.referral_code.ReferralTransactionsDto;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;


import java.math.BigDecimal;
import java.util.Set;


public interface ReferralCodeService {

    ReferralCodeDto getForUser();

    ReferralCodeDto get(String referralCode);

    PaginatedResponse<ReferralCodeDto> getList(int pageNo, int pageSize);

    PaginatedResponse<ReferralTransactionsDto> getChilds(ReferralTransactionsType type, int pageNo, int pageSize);

    void send(String phoneNumber, String parentReferralCode);

    void activate(String referralCode);
    void deActivate(String referralCode);

    ReferralCodeDto validateReferralOtp(String referralOtpToken);

    BigDecimal shareRevenueForOrder(OrdersEntity ordersEntity);

    BigDecimal calculateTheWithdrawValueFromReferralBalance(Long userId, BigDecimal orderAmount);

    void withDrawFromReferralWallet(MetaOrderEntity order);

    BigDecimal getReferralConfigValue(String referralCode, ReferralCodeType type);

    Long saveReferralTransactionForOrderDiscount(OrdersEntity ordersEntity);

    void addReferralDiscountForSubOrders(String referralCode, Set<OrdersEntity> subOrders, Long userId);

    ReferralStatsDto getStats();
    }
