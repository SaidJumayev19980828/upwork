package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ShipmentRequest {
    @JsonProperty("WarehouseName")
    private String shopName;
    @JsonProperty("PickupDueDate")
    private LocalDateTime pickupDate;
    @JsonProperty("Package_Serial")
    private String serial;
    @JsonProperty("Description")
    private String description;
    @JsonProperty("WarehouseName")
    private String shopName;
}
