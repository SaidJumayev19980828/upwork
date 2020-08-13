
package com.nasnav.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

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

    @JsonProperty("logo")
    private String logo;
    private String banner;
    @JsonProperty("open")
    private OpenWorkingDays openWorkingDays;

    @JsonProperty("place_id")
    private String placeId;

    private List<OrganizationImagesRepresentationObject> images;
}
