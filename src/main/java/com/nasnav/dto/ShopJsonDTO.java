package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value = "Shops data")
@JsonPropertyOrder({"org_id", "id", "name", "address_country", "address_street", "address_streetno",
        "address_floor", "address_lat", "address_lng", "brand_id", "logo", "banner", "photo"})

public class ShopJsonDTO extends BaseJsonDTO{

    @JsonProperty("org_id")
    private Long orgId;

    private Long id;

    private String name;

    @JsonProperty("address_country")
    private String country;

    @JsonProperty("address_street")
    private String street;

    @JsonProperty("address_streetno")
    private String streetNumber;

    @JsonProperty("address_floor")
    private String floor;

    @JsonProperty("address_lat")
    private BigDecimal lat;

    @JsonProperty("address_lng")
    private BigDecimal lng;

    @JsonProperty("brand_id")
    private Long brandId;

    private String logo;

    private String banner;

    @Override
    protected void initRequiredProperties() { }

    void setBanner(String banner) {
        setPropertyAsUpdated("banner");
        this.banner = banner;
    }

    void setLogo(String logo) {
        setPropertyAsUpdated("logo");
        this.logo = logo;
    }

    public void setOrgId(Long orgId) {
        setPropertyAsUpdated("orgId");
        this.orgId = orgId;
    }

    public void setName(String name) {
        setPropertyAsUpdated("name");
        this.name = name;
    }

    void setCountry(String country) {
        setPropertyAsUpdated("country");
        this.country = country;
    }

    void setStreet(String street) {
        setPropertyAsUpdated("street");
        this.street = street;
    }

    void setStreetNumber(String streetNumber) {
        setPropertyAsUpdated("streetNumber");
        this.streetNumber = streetNumber;
    }

    void setFloor(String floor) {
        setPropertyAsUpdated("floor");
        this.floor = floor;
    }

    void setLat(BigDecimal lat) {
        setPropertyAsUpdated("lat");
        this.lat = lat;
    }

    void setLng(BigDecimal lng) {
        setPropertyAsUpdated("lng");
        this.lng = lng;
    }

    void setBrandId(Long brandId) {
        setPropertyAsUpdated("brandId");
        this.brandId = brandId;
    }

}
