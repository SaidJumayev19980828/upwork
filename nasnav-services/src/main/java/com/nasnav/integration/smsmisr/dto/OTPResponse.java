package com.nasnav.integration.smsmisr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OTPResponse(@JsonProperty("Code") String code,
                          @JsonProperty("SMSID") String smsId,
                          @JsonProperty("Cost") String cost){
}
