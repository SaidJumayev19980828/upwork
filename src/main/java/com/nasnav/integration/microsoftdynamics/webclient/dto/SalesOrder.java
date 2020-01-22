package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesOrder {
    @JsonProperty("CountryID")
    private String countryId;
    @JsonProperty("CustomerID")
    private String customerId;
    @JsonProperty("Address")
    private String address;
    @JsonProperty("City_Code")
    private String cityCode;
    @JsonProperty("Total_Order_Discount")
    private BigDecimal totalOrderDiscount;
    @JsonProperty("Total")
    private BigDecimal total;
    @JsonProperty("Shipping_fees")
    private BigDecimal shippingFees;
    @JsonProperty("COD_Fee")
    private BigDecimal cashOnDeliveryFee;
    @JsonProperty("Subtotal")
    private BigDecimal subTotal;
    @JsonProperty("InventSite")
    private String inventSite;
    @JsonProperty("Store")
    private String store;
    @JsonProperty("PaymentMethod")
    private String paymentMethod;
    @JsonProperty("CodCode")
    private String codCode;
    @JsonProperty("CODFeeAmount")
    private BigDecimal codFeeAmount;
    @JsonProperty("ShippingfeesCode")
    private String shippingFeesCode;
    @JsonProperty("Items")
    private List<SalesOrderItem> items;
}
