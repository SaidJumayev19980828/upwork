package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShopThreeSixtyRepresentationObject {

    private Long id;

    @JsonProperty( "web_json_data")
    private String webJsonData;

    @JsonProperty("url")
    private String url;

    @JsonProperty("scene_asset_bundle")
    private String sceneAssetBundle;

    @JsonProperty("scene_name")
    private String sceneName;

    @JsonProperty("mobile_json_data")
    private String mobileJsonData;

    @JsonProperty("published")
    private boolean published;

    @JsonProperty("preview_json_data")
    private String previewJsonSata;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
