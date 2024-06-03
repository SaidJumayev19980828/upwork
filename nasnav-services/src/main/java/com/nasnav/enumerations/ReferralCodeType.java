package com.nasnav.enumerations;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public enum ReferralCodeType {

    @Schema(description = "The type at which user activated referral code by validating the otp")
    REFERRAL_ACCEPT_REVENUE,

    @Schema(description = "Value of discount on percentage ")
    ORDER_DISCOUNT_PERCENTAGE,

    @Schema(description = "Share revenue value on percentage to the parent referral")
    SHARE_REVENUE_PERCENTAGE,

    @Schema(description = "The type for adding date range for any registration to referral")
    PARENT_REGISTRATION,

    @Schema(description = "The type for adding date range at which child can register to the referral")
    CHILD_REGISTRATION,

    @Schema(description = "The type for adding date range at which user can use his referral wallet to pay")
    PAY_WITH_REFERRAL_WALLET;
}
