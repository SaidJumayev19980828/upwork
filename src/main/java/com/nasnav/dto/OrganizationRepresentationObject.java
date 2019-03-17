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
        "description",
        "type",
        "brands",
        "social",
        "themes"
})
@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationRepresentationObject extends BaseRepresentationObject{

    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("type")
    private String type;
    @JsonProperty("brands")
    private List<Organization_BrandRepresentationObject> brands = null;
    @JsonProperty("social")
    private SocialRepresentationObject social;
    @JsonProperty("themes")
    private OrganizationThemesRepresentationObject themes;
}

