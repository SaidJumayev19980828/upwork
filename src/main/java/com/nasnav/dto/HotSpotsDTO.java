package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class HotSpotsDTO {

    @JsonProperty("iconindex")
    private Long iconIndex;
    private List<String> images;
    private String price;
    private String sceneId;
    private Long pitch;
    private String text;
    private String type;
    private String title;
    private Long yaw;
}
