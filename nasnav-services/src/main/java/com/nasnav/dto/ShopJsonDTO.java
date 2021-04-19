package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(name = "Shops data")
@JsonPropertyOrder({"id", "name", "address", "brand_id", "logo", "banner", "photo"})
@EqualsAndHashCode(callSuper=false)
public class ShopJsonDTO extends BaseJsonDTO{

    private Long id;

    private String name;

    private AddressDTO address;

    @JsonProperty("brand_id")
    private Long brandId;

    private String logo;

    private String banner;

    @JsonProperty("place_id")
    private String placeId;
    
    @JsonProperty("is_warehouse")
    private Boolean isWarehouse;

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

    public void setPlaceId(String placeId) {
        setPropertyAsUpdated("placeId");
        this.placeId = placeId;
    }
    
    
    public void setIsWarehouse(Boolean isWarehouse) {
    	setPropertyAsUpdated("isWarehouse");
    	this.isWarehouse = isWarehouse;
    }
    

}
