package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value = "Shops data")
@JsonPropertyOrder({"org_id", "id", "name", "address", "brand_id", "logo", "banner", "photo"})

public class ShopJsonDTO extends BaseJsonDTO{

    @JsonProperty("org_id")
    private Long orgId;

    private Long id;

    private String name;

    private AddressDTO address;

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

    public void setBrandId(Long brandId) {
        setPropertyAsUpdated("brandId");
        this.brandId = brandId;
    }

    public void setAddress(AddressDTO address) {
        setPropertyAsUpdated("address");
        this.address = address;
    }

}
