package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class OrganizationDTO {

    @ApiModel(value = "Organization Creation Data")
    public static class OrganizationCreationDTO {
        @ApiModelProperty(value = "Organization's name", example = "Super Pharm", required = true)
        @JsonProperty("name")
        public String name;

        @ApiModelProperty(value = "Url-compatible name, used as part of the URL path for organization shop", example = "super-pharm", required = true)
        @JsonProperty("p_name")
        public String pname;
    }

    @ApiModel(value = "Organization Modification Data")
    public static class OrganizationModificationDTO {
        @ApiModelProperty(value = "organization id", example = "123", required = true)
        @JsonProperty("org_id")
        public Long organizationId;

        @ApiModelProperty(value = "short description about the organization", example = "The company was  established  in  1948.  At that time..... ")
        @JsonProperty("description")
        public String description;

        @ApiModelProperty(value = "theme id used in the organization", example = "123")
        @JsonProperty("theme_id")
        public Integer themeId;

        @ApiModelProperty(value = "URL to twitter account", example = "https://www.twitter.com/fortunestores")
        @JsonProperty("social_twitter")
        public String socialTwitter;

        @ApiModelProperty(value = "URL to facebook account", example = "https://www.facebook.com/fortune.stores11/")
        @JsonProperty("social_facebook")
        public String socialFacebook;

        @ApiModelProperty(value = "URL to instagram account", example = "https://instagram.com/fortunestores")
        @JsonProperty("social_instagram")
        public String socialInstagram;
    }
}
