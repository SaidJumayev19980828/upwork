package com.nasnav.dto.referral_code;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import com.nasnav.enumerations.ReferralCodeStatus;
import com.nasnav.enumerations.ReferralCodeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ReferralCodeDto {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String referrerEmail;
    private Map<ReferralCodeType, BigDecimal> constraints;
    private ReferralCodeStatus status;
}
