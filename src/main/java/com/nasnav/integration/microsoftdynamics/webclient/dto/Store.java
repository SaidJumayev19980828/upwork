package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Store {
    @JsonProperty("shop_id")
    private String id;

    @JsonProperty("shop_name")
    private String name;
}
