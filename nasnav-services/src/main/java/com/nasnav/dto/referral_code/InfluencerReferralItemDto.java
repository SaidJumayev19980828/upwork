package com.nasnav.dto.referral_code;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class InfluencerReferralItemDto {

    private Integer quantity;
    private BigDecimal price;
    private Long productId;
    private BigDecimal discount;
    private String influencerReferralDiscount;
}
