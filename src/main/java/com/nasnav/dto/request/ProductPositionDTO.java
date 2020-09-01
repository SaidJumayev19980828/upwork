package com.nasnav.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@AllArgsConstructor
public class ProductPositionDTO {

    @JsonProperty(value = "id")
    private Long productId;
    private Long floorId;
    private Long sectionId;
    private Long sceneId;
    private Float pitch;
    private Float yaw;
    private Integer productType;
}
