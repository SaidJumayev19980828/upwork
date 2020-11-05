package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nasnav.dto.request.organization.SettingDTO;
import com.nasnav.dto.response.OrgThemeRepObj;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "description",
        "type",
        "currency",
        "theme_id",
        "ecommerce",
        "google_token",
        "brands",
        "social",
        "themes",
        "settings"
})

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Organization")
public class OrganizationRepresentationObject extends BaseRepresentationObject{

    @ApiModelProperty(value = "ID key identifying the organization", readOnly = true)
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty(value = "Sub dir present in URL (e.g. nasnav.com/myshop/")
    @JsonProperty("sub_dir")
    private Long subDir = 0L;

    @ApiModelProperty(value = "Name of the organization")
    @JsonProperty("name")
    private String name;

    @ApiModelProperty(value = "URL-friendly Name of the organization")
    @JsonProperty("p_name")
    private String pname;

    @ApiModelProperty(value = "Description")
    @JsonProperty("description")
    private String description;

    @ApiModelProperty(value = "Type of the organization (shop, services, etc.)")
    @JsonProperty("type")
    private String type;

    @ApiModelProperty(value = "theme id used in the organization")
    @JsonProperty("theme_id")
    private String themeId;

    @ApiModelProperty(value = "Brands carried by the organization")
    @JsonProperty("brands")
    private List<Organization_BrandRepresentationObject> brands = null;

    @ApiModelProperty(value = "Social websites (facebook, twitter)")
    @JsonProperty("social")
    private SocialRepresentationObject social;

    @ApiModelProperty(value = "Website theme to use to display organization web pages")
    @JsonProperty("themes")
    private OrganizationThemesRepresentationObject themes;

    @ApiModelProperty(value = "All the images related to the organization")
    private List<OrganizationImagesRepresentationObject> images;

    private OrgThemeRepObj theme;

    @JsonProperty("extra_info")
    private Map info;

    @ApiModelProperty(value = "E-commerce functionality of the website")
    @JsonProperty("ecommerce")
    private Integer ecommerce;

    @ApiModelProperty(value = "Token used to identify websites at Google")
    @JsonProperty("google_token")
    private String googleToken;

    private String currency;

    private Map<String,String> settings;

}

