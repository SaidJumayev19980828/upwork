package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ThemeDTO extends BaseRepresentationObject{

    private String uid;
    private String name;

    @JsonProperty("preview_image")
    private String previewImage;

    @JsonProperty("default_settings")
    private String defaultSettings;

    @JsonProperty("theme_class_id")
    private Integer themeClassId;
}
