package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value = "Shops data")
@JsonPropertyOrder({"org_id", "id", "name", "address_country", "address_street", "address_streetno",
        "address_floor", "address_lat", "address_lng", "mall_id", "brand_id", "logo", "banner", "photo"})

public class ShopJsonDTO {

    @ApiModelProperty(value = "Organization Id that the shop belong to", example = "12345", required = true)
    @JsonProperty("org_id")
    private Long orgId;

    @ApiModelProperty(value = "shop Id", example = "12345")
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty(value = "Shop Name", example = "Eventure")
    @JsonProperty("shop_name")
    private String name;

    @ApiModelProperty(value = "Shop Address Country", example = "Egypt")
    @JsonProperty("address_country")
    private String country;

    @ApiModelProperty(value = "Shop Address Street", example = "Omar Bin Khatab")
    @JsonProperty("address_street")
    private String street;

    @ApiModelProperty(value = "Shop Address Street#", example = "24")
    @JsonProperty("address_streetno")
    private String streetNumber;

    @ApiModelProperty(value = "Shop Address floor", example = "Second")
    @JsonProperty("address_floor")
    private String floor;

    @ApiModelProperty(value = "Shop Address Latitude", example = "30.0595581")
    @JsonProperty("address_lat")
    private BigDecimal lat;

    @ApiModelProperty(value = "Shop Address Longitude", example = "31.2234449")
    @JsonProperty("address_lng")
    private BigDecimal lng;

    @ApiModelProperty(value = "Shop Mall Id", example = "12345")
    @JsonProperty("mall_id")
    private Long mallId;

    @ApiModelProperty(value = "Shop Brand Id", example = "12345")
    @JsonProperty("brand_id")
    private Long brandId;

    @ApiModelProperty(value = "Shop or associated brand logo (image)", example = "/brands/hugo_logo.jpg")
    @JsonProperty("logo")
    private String logo;

    @ApiModelProperty(value = "Shop individual banner (image)", example = "/banners/banner_256.jpg")
    @JsonProperty("banner")
    private String banner;

    @ApiModelProperty(value = "shop front photo (image)", example = "/photos/photo_512.jpg")
    @JsonProperty("photo")
    private String photo;
}
