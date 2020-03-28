package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductPositionsDataDTO {

    @JsonProperty("floor_id")
    private Long floorId;
    @JsonProperty("section_id")
    private Long sectionId;
    @JsonProperty("scene_id")
    private Long sceneId;
    @JsonProperty("product_id")
    private Long productId;
    private Long pitch;
    private Long yaw;
}
