package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public abstract class AbstractResponse {
    @JsonProperty("CoreValue")
    private Object coreValue;
    @JsonProperty("IsErrorState")
    private Boolean isErrorState;
    @JsonProperty("ErrorDescription")
    private String errorDescription;
    @JsonProperty("ErrorMetadata")
    private Integer errorMetadata;
}
