package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
public class ProductPositionDTO {
    private Long id;
    private Integer floorNumber;
    private Long sectionId;
    private Long sceneId;
    private Float pitch;
    private Float yaw;
    private Integer productType;
}
