package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ThemeRepresentationObject {

    private Integer id;

    private String name;

    @JsonProperty("preview_image")
    private String previewImage;

    @JsonProperty("default_settings")
    private String defaultSettings;

    @JsonProperty("current_settings")
    private String currentSettings;

    @JsonProperty("theme_class_id")
    private Integer themeClassId;
}
