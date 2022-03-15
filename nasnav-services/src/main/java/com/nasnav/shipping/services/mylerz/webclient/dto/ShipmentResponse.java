package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ShipmentResponse extends AbstractResponse{
    @JsonProperty("Value")
    private ShipmentResponseDetails details;
}
