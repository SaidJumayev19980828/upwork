package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.enumerations.YeshteryState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Data
public class OrganizationDTO {

    @Schema(name = "Organization Creation Data")
    public static class OrganizationCreationDTO {

        public Long id;

        @Schema(name = "Organization's name", example = "Super Pharm", required = true)
        public String name;

        @Schema(name = "Url-compatible name, used as part of the URL path for organization shop", example = "super-pharm", required = true)
        @JsonProperty("p_name")
        public String pname;

        @Schema(name = "E-commerce functionality of the website", example = "1")
        @JsonProperty("ecommerce")
        public Integer ecommerce;

        @Schema(name = "Tag/token used to identify websites at Google", example = "zaSyBMV3KncHiZJKbFHgp6rJ34a2w5W5nOWdf")
        @JsonProperty("google_token")
        public String googleToken;


        @JsonProperty("currency_iso")
        public Integer currencyIso;

        @JsonProperty("yeshtery_state")
        @Schema(name = "state of the organization on yeshtery application", example = "ACTIVE")
        public YeshteryState yeshteryState;
    }

    @Schema(name = "Organization Modification Data")
    public static class OrganizationModificationDTO {
        @Schema(name = "organization id", example = "123", required = true)
        @JsonProperty("org_id")
        public Long organizationId;

        @Schema(name = "short description about the organization", example = "The company was  established  in  1948.  At that time..... ")
        @JsonProperty("description")
        public String description;

        @Schema(name = "theme id used in the organization", example = "123")
        @JsonProperty("theme_id")
        public Integer themeId;

        @Schema(name = "URL to twitter account", example = "https://www.twitter.com/fortunestores")
        @JsonProperty("social_twitter")
        public String socialTwitter;

        @Schema(name = "URL to facebook account", example = "https://www.facebook.com/fortune.stores11/")
        @JsonProperty("social_facebook")
        public String socialFacebook;

        @Schema(name = "URL to instagram account", example = "https://instagram.com/fortunestores")
        @JsonProperty("social_instagram")
        public String socialInstagram;

        @JsonProperty("social_youtube")
        public String socialYoutube;

        @JsonProperty("social_linkedin")
        public String socialLnkedin;

        @JsonProperty("social_pinterest")
        public String socialPinterest;

        @JsonProperty("extra_info")
        public Map<?,?> info;

        @JsonProperty("yeshtery_state")
        public YeshteryState yeshteryState;
    }
}
