package com.nasnav.integration.smsMis.dto;

import java.io.Serializable;

public record OTPResponse(String code, String SMSID, String cost){
}
