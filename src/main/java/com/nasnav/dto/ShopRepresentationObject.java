
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
        "address",
        "logo",
        "banner",
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
    private String pName;

    private Address address;

    @JsonProperty("logo")
    private String logo;
    private String banner;
    @JsonProperty("open")
    private OpenWorkingDays openWorkingDays;

}
