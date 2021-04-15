package com.nasnav.shipping.services.bosta.webclient.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CreateDeliveryResponse {
    @JsonProperty("_id")
    private String id;
    private String trackingNumber;
    private String message;
    private State state;
    private Log[] logs;
}
