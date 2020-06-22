package com.nasnav.shipping.services.bosta.webclient.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class SubAccount {
    @JsonProperty("_id")
    private String id;
    private String name;
    private String phone;
    private Address address;
}
