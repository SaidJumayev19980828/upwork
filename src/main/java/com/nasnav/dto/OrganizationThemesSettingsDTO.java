package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrganizationThemesSettingsDTO extends BaseRepresentationObject {

    private Integer id;
    @JsonProperty("theme_id")
    private Integer themeId;
    private String settings;
}
