package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesOrder {
    private String countryId;
    private String customerId;
    private String address;
    private String cityCode;
    private BigDecimal totalOrderDiscount;
    private BigDecimal total;
    private BigDecimal shippingFees;
    @JsonProperty("COD_Fee")
    private BigDecimal cashOnDeliveryFee;
    private BigDecimal Subtotal;
    private String inventSite;
    private String store;
    private String paymentMethod;
    private String codCode;
    private BigDecimal codFeeAmount;
    private String shippingFeesCode;
    private List<SalesOrderItem> items;
}
