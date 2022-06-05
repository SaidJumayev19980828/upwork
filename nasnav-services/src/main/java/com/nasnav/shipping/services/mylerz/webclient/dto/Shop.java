package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class Shop {
    @JsonProperty("MerchantName")
    private String merchantName;
    @JsonProperty("Name")
    private String shopName;
    @JsonProperty("Zone")
    private Zone zone;
    @JsonProperty("SubZone")
    private Zone subZone;
    private List<Zone> hubs;
    @JsonProperty("Latitude")
    private BigDecimal latitude;
    @JsonProperty("Longitude")
    private BigDecimal longitude;
    @JsonProperty("Address")
    private String address;
    @JsonProperty("PhoneNumber")
    private String phoneNumber;
    @JsonProperty("ContactName")
    private String contactName;
}
