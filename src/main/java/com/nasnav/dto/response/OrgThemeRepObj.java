package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

import java.util.Map;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgThemeRepObj {
    private String uid;
    private String name;
    private String previewImage;
    private Map defaultSettings;
    private Map settings;
    @JsonProperty("theme_class_id")
    private Integer id;

    public OrgThemeRepObj(String uid, String name, String previewImage, String defaultSettings,
                            String settings,Integer id) {
        this.uid = uid;
        this.name = name;
        this.previewImage = previewImage;
        this.defaultSettings = new JSONObject(defaultSettings).toMap();
        this.settings = new JSONObject(settings).toMap();
        this.id = id;
    }
}
