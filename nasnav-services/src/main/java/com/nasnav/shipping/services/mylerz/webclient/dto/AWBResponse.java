package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AWBResponse extends AbstractResponse{
    @JsonProperty("Value")
    private String value;
}
