package com.nasnav.service;

import com.nasnav.dto.referral_code.ReferralSettingsDto;
import com.nasnav.enumerations.ReferralCodeType;

import java.math.BigDecimal;
import java.util.Map;

public interface ReferralSettingsService {

    ReferralSettingsDto get();

    ReferralSettingsDto create(ReferralSettingsDto referralSettingsDto);

    void update(ReferralSettingsDto referralSettingsDto);

    Map<ReferralCodeType, BigDecimal> getValue(ReferralCodeType referralCodeType);


}
