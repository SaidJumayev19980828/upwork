package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class OrganizationThemesSettingsDTO extends BaseRepresentationObject {

    private Integer id;
    @JsonProperty("theme_id")
    private String themeId;
    private String settings;
}
