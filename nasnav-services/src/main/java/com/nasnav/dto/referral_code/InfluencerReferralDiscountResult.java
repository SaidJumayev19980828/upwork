package com.nasnav.dto.referral_code;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class InfluencerReferralDiscountResult {

    private BigDecimal totalDiscount;
    private List<AppliedReferralDiscount> appliedReferralDiscountList;

}
