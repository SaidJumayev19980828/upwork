
package com.nasnav.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "p_name",
        "logo",
        "banner",
        "place_id",
        "address",
        "open"
})

@Data
@EqualsAndHashCode(callSuper=true)
public class ShopRepresentationObject extends BaseRepresentationObject{

    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("p_name")
    private String pname;

    private AddressRepObj address;

    private String logo;
    @JsonProperty("darl_logo")
    private String darkLogo;
    private String banner;
    @JsonProperty("open")
    private OpenWorkingDays openWorkingDays;

    @JsonProperty("place_id")
    private String placeId;

    private List<OrganizationImagesRepresentationObject> images;
    
    @JsonProperty("is_warehouse")
    private Boolean isWarehouse;

    private Integer priority;

    @JsonIgnore
    private Long addressId;

    @JsonIgnore
    private Long cityId;

    @JsonProperty("has_360")
    public Boolean has360;

    @JsonProperty("variant_ids")
    Set<Long> variantIds;
}
