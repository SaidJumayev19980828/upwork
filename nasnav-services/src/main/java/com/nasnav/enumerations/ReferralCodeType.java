package com.nasnav.enumerations;

import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

import static com.nasnav.exceptions.ErrorCodes.LOY$PARAM$0004;
import static com.nasnav.exceptions.ErrorCodes.REF$PARAM$0001;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Getter
@AllArgsConstructor
public enum ReferralCodeType {
    REFERRAL_ACCEPT_REVENUE,
    SHARE_REVENUE_PERCENTAGE,
    ORDER_DISCOUNT_PERCENTAGE;
}
