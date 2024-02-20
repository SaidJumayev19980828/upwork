package com.nasnav.integration;

import com.nasnav.integration.smsMis.dto.OTPDto;
import com.nasnav.integration.smsMis.dto.OTPResponse;
import org.springframework.stereotype.Service;


public interface MobileOTPService {
    String send(OTPDto otpDto);

}
