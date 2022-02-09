package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryFeeRequest {
    @JsonProperty("CODValue")
    private BigDecimal codValue;
    @JsonProperty("WarehouseName")
    private String shopName;
    @JsonProperty("CustomerZoneCode")
    private String deliveryAreaId;
    @JsonProperty("PackageWeight")
    private Double weight;
    @JsonProperty("CustomerZoneCode")
    private String areaId;
    @JsonProperty("PackageServiceTypeCode")
    private String serviceTypeCode;
    @JsonProperty("PackageServiceCode")
    private String serviceCode;
    @JsonProperty("PaymentTypeCode")
    private String paymentTypeCode;
    @JsonProperty("ServiceCategoryCode")
    private String ServiceCategoryCode;
}
