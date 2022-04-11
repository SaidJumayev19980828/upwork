package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class Organization_BrandRepresentationObject extends BaseRepresentationObject {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("p_name")
    private String pname;
    @JsonProperty("category_id")
    private Integer categoryId;
    @JsonProperty("logo_url")
    private String logoUrl;
    @JsonProperty("banner")
    private String bannerImage;
    @JsonProperty("cover_url")
    private String coverUrl;
    private Integer priority;
    @JsonProperty("org_name")
    private String orgName;

}