package com.nasnav.enumerations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReferralCodeType {
    REFERRAL_ACCEPT_REVENUE,
    ORDER_DISCOUNT_PERCENTAGE,
    SHARE_REVENUE_PERCENTAGE,
    PARENT_REGISTRATION,
    CHILD_REGISTRATION,
    PAY_WITH_REFERRAL_WALLET
    ;
}
