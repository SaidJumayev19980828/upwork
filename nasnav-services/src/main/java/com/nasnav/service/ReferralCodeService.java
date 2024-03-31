package com.nasnav.service;

import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.*;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface ReferralCodeService {

    ReferralCodeDto getForUser();

    ReferralCodeDto get(String referralCode);

    PaginatedResponse<ReferralCodeDto> getList(int pageNo, int pageSize);

    PaginatedResponse<ReferralTransactionsDto> getChilds(ReferralTransactionsType type, String dateFrom, String dateTo, int pageNo, int pageSize);

    void send(String phoneNumber, String parentReferralCode);

    void resend();

    void activate(String referralCode);
    void deActivate(String referralCode);

    ReferralCodeDto validateReferralOtp(String referralOtpToken);

    BigDecimal shareRevenueForOrder(OrdersEntity ordersEntity);

    BigDecimal calculateTheWithdrawValueFromReferralBalance(Long userId, BigDecimal orderAmount);

    void withDrawFromReferralWallet(MetaOrderEntity order);

    ReferralConstraints getReferralConfigValue(String referralCode, ReferralCodeType type);

    void saveReferralTransactionForOrderDiscount(OrdersEntity ordersEntity);

    BigDecimal calculatePayWithReferralOnOrders(Set<OrdersEntity> subOrders, Long userId, BigDecimal discounts, BigDecimal total, BigDecimal subTotal);

    void addReferralDiscountForSubOrders(String referralCode, Set<OrdersEntity> subOrders, Long userId);

    public BigDecimal calculateReferralDiscountForCartItems(String referralCode, List<CartItem> items, Long userId);

    ReferralStatsDto getStats();

    boolean checkIntervalDateForCurrentOrganization(ReferralCodeType referralCodeType);

    void returnWithdrawAmountToUserReferralWallet(MetaOrderEntity metaOrder);
}
