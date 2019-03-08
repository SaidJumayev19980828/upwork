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
    public Long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("description")
    public String description;
    @JsonProperty("type")
    public String type;
    @JsonProperty("brands")
    public List<BrandRepresentationObject> brands = null;
    @JsonProperty("social")
    public SocialRepresentationObject social;
    @JsonProperty("themes")
    public OrganizationThemesRepresentationObject themes;
}

