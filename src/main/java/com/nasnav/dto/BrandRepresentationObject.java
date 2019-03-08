package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "p_name",
        "display_name",
        "category_id",
        "logo_url"
})
@Data
public class BrandRepresentationObject extends BaseRepresentationObject {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("p_name")
    private String pName;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("category_id")
    private Integer categoryId;
    @JsonProperty("logo_url")
    private String logoUrl;

}