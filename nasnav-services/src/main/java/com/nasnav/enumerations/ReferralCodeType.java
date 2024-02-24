package com.nasnav.enumerations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReferralCodeType {
    REFERRAL_ACCEPT_REVENUE,
    SHARE_REVENUE_PERCENTAGE,
    ORDER_DISCOUNT_PERCENTAGE;
}
