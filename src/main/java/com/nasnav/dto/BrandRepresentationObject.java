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
    public Integer id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("p_name")
    public String pName;
    @JsonProperty("display_name")
    public String displayName;
    @JsonProperty("category_id")
    public Integer categoryId;
    @JsonProperty("logo_url")
    public String logoUrl;

}