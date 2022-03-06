package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShipmentResponseDetails {
    @JsonProperty("PickupOrderCode")
    private String pickupOrderCode;
    @JsonProperty("PickupDateTime")
    private LocalDateTime pickupDateTime;
    @JsonProperty("ErrorCode")
    private String errorCode;
    @JsonProperty("ErrorMessage")
    private String errorMessage;
    @JsonProperty("Packages")
    List<Package> packages;
}
