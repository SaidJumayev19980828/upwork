package com.nasnav.service;

import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeCreateResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

import static java.math.RoundingMode.FLOOR;

public interface ReferralCodeService {

    ReferralCodeDto get(Long id);
    ReferralCodeDto get(String referralCode);

    PaginatedResponse getList(int pageNo, int pageSize);
    ReferralCodeCreateResponse create(ReferralCodeDto referralCodeDto) throws RuntimeBusinessException;

    void send(String phoneNumber, String parentReferralCode);

    void update(ReferralCodeDto referralCodeDto);

    void activate(String referralCode);
    void deActivate(String referralCode);

    void delete(String referralCode);

    String validateReferralOtp(String referralOtpToken);

    BigDecimal shareRevenueForOrder(OrdersEntity ordersEntity);

    BigDecimal getReferralConfigValue(String referralCode, ReferralCodeType type);

    Long saveReferralTransactionForOrderDiscount(OrdersEntity ordersEntity);

    void addReferralDiscountForSubOrders(String referralCode, Set<OrdersEntity> subOrders, Long userId);

    }
