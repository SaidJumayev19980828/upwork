package com.nasnav.dto.referral_code;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AppliedReferralDiscount {
    private Long itemId;
    private BigDecimal discount;
}
