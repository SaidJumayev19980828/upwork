package com.nasnav.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductPositionDTO {

    private Long id;
    private Long floor_id;
    private Long section_id;
    private Long scene_id;
    private Float pitch;
    private Float yaw;
    private Integer product_type;
}
