package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShipmentRequest {
    @JsonProperty("WarehouseName")
    private String shopName;
    @JsonProperty("PickupDueDate")
    private LocalDateTime pickupDate;
    @JsonProperty("Package_Serial")
    private String serial;
    @JsonProperty("Description")
    private String description;
    @JsonProperty("Total_Weight")
    private Double totalWeight;
    @JsonProperty("Service_Type")
    private String serviceType;
    @JsonProperty("Service")
    private String service;
    @JsonProperty("ServiceDate")
    private LocalDateTime serviceDate;
    @JsonProperty("Service_Category")
    private String serviceCategory;
    @JsonProperty("Payment_Type")
    private String paymentType;
    @JsonProperty("COD_Value")
    private BigDecimal codValue;
    @JsonProperty("Pieces")
    private List<Piece> pieces;
    @JsonProperty("Customer_Name")
    private String customerName;
    @JsonProperty("Mobile_No")
    private String mobileNo;
    @JsonProperty("Building_No")
    private Integer buildingNo;
    @JsonProperty("Street")
    private String street;
    @JsonProperty("Floor_No")
    private Integer floorNo;
    @JsonProperty("Apartment_No")
    private String apartmentNo;
    @JsonProperty("Country")
    private String country;
    @JsonProperty("City")
    private String city;
    @JsonProperty("Neighborhood")
    private String neighborhood;
    @JsonProperty("District")
    private String district;
    @JsonProperty("GeoLocation")
    private String geoLocation;
    @JsonProperty("Address_Category")
    private String addressCategory;
    @JsonProperty("Telephone")
    private String telephone;
    @JsonProperty("Address2")
    private String address2;
    @JsonProperty("CustVal")
    private String custVal;
    @JsonProperty("Currency")
    private String currency;
}
