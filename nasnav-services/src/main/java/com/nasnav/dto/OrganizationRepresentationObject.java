package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.response.OrgThemeRepObj;
import com.nasnav.enumerations.DiscountStrategies;
import com.nasnav.enumerations.VideoChatOrgState;
import com.nasnav.enumerations.YeshteryState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.Column;
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
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Schema(name = "Organization")
public class OrganizationRepresentationObject extends BaseRepresentationObject{

    @Schema(name = "ID key identifying the organization", readOnly = true)
    private Long id;

    @Schema(name = "Sub dir present in URL (e.g. nasnav.com/myshop/")
    private Long subDir = 0L;

    @Schema(name = "Name of the organization")
    private String name;

    @Schema(name = "URL-friendly Name of the organization")
    @JsonProperty("p_name")
    private String pname;

    @JsonProperty("google_analytics_site_id")
    private String googleAnalyticsSiteId;

    @Schema(name = "Description")
    private String description;

    @Schema(name = "Type of the organization (shop, services, etc.)")
    private String type;

    @Schema(name = "theme id used in the organization")
    private String themeId;

    @Schema(name = "Brands carried by the organization")
    private List<Organization_BrandRepresentationObject> brands = null;

    @Schema(name = "Social websites (facebook, twitter)")
    private SocialRepresentationObject social;

    @Schema(name = "Website theme to use to display organization web pages")
    private OrganizationThemesRepresentationObject themes;

    @Schema(name = "All the images related to the organization")
    private List<OrganizationImagesRepresentationObject> images;

    private OrgThemeRepObj theme;

    @JsonProperty("extra_info")
    private Map info;

    @Schema(name = "E-commerce functionality of the website")
    private Integer ecommerce;

    @Schema(name = "Token used to identify websites at Google")
    private String googleToken;

    private String facebookToken;

    private String currency;
    private Integer currencyIso;
    private Map<String,String> settings;
    private Integer matomoSiteId;
    private String pixelSiteId;
    private Boolean yeshteryState;
    private String domain;
    private Integer priority;
    private boolean enableVideoChat;
    private Long bankAccountId;
    private String shortDescription;
    private String openingHours;
    private Map<DiscountStrategies,Boolean> strategies;

}

