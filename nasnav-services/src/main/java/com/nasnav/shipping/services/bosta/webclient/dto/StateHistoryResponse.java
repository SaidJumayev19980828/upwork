package com.nasnav.shipping.services.bosta.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StateHistoryResponse {
    @JsonProperty("_id")
    private String id;
    private String trackingNumber;
    private StateHistory stateHistory;
}
