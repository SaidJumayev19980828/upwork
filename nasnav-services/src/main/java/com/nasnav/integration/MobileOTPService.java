package com.nasnav.integration;

import com.nasnav.integration.smsmisr.dto.OTPDto;


public interface MobileOTPService {
    String send(OTPDto otpDto);

}
