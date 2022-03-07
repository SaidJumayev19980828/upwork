package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WarehousesResponse extends AbstractResponse{
    @JsonProperty("Value")
    private List<Shop> shops;
}
