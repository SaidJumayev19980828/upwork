package com.nasnav.dto.request.organization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.YeshteryState;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrganizationModificationDTO {
    private String name;
    private Long orgId;
    private String description;
    private String shortDescription;
    private String openingHours;
    private Integer themeId;
    private String socialTwitter;
    private String socialFacebook;
    private String socialInstagram;
    private String socialYoutube;
    private String socialLinkedin;
    private String socialPinterest;
    private String socialTiktok;
    private String socialWhatsapp;
    @JsonProperty("extra_info")
    private Map<?,?> info;
    private YeshteryState yeshteryState;
}
