package com.nasnav.dto.referral_code;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfluencerReferralDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String confirmPassword;
    private String referralCode;
    @Schema(description = "wallet balance for the influencer")
    private BigDecimal cashback;
    private InfluencerReferralConstraints constraints;
}
