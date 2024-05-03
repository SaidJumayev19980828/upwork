package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ExchangeRateResponse {

        private String result;
        private String provider;
        private String documentation;
        private String termsOfUse;

        @JsonProperty("time_last_update_unix")
        private long timeLastUpdateUnix;

        @JsonProperty("time_last_update_utc")
        private String timeLastUpdateUtc;

        @JsonProperty("time_next_update_unix")
        private long timeNextUpdateUnix;

        @JsonProperty("time_next_update_utc")
        private String timeNextUpdateUtc;

        @JsonProperty("time_eol_unix")
        private long timeEolUnix;

        @JsonProperty("base_code")
        private String baseCode;

        @JsonDeserialize(contentAs = BigDecimal.class)
        private Map<String, BigDecimal> rates;
}

