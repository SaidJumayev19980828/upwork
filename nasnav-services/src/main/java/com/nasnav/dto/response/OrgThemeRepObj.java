package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.ThemeEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class OrgThemeRepObj {
    private String uid;
    private String name;
    private String previewImage;
    private Map defaultSettings;
    @JsonIgnore
    private String defaultSettingsString;
    private Map settings;
    @JsonProperty("theme_class_id")
    private Integer id;

    public OrgThemeRepObj(ThemeEntity themeEntity) {
        this.uid = themeEntity.getUid();
        this.name = themeEntity.getName();
        this.previewImage = themeEntity.getPreviewImage();
        this.defaultSettingsString = themeEntity.getDefaultSettings();
        this.id = themeEntity.getThemeClassEntity().getId();
    }
}
