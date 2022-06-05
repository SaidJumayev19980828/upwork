package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Zone {
    @JsonProperty("Name")
    private String name;
    private String code;
}
