package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubAreasRepObj extends BaseRepresentationObject{

    private Long id;
    private String name;
    private BigDecimal longitude;
    private BigDecimal latitude;
    @JsonProperty("area_id")
    private Long areaId;
    private Long orgId;
}
