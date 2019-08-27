package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "p_name",
        "display_name",
        "category_id",
        "logo_url",
        "banner"
})

@Data
@EqualsAndHashCode(callSuper=true)
public class Organization_BrandRepresentationObject extends BaseRepresentationObject {

    @JsonProperty("brand_id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("p_name")
    private String pname;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("category_id")
    private Integer categoryId;
    @JsonProperty("logo")
    private String logoUrl;
    @JsonProperty("banner")
    private String bannerImage;

}