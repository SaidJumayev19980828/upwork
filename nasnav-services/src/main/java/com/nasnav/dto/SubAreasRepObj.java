package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
public class SubAreasRepObj extends BaseRepresentationObject{

    private Long id;
    private String name;
    private BigDecimal longitude;
    private BigDecimal latitude;
    @JsonProperty("area_id")
    private Long areaId;
    private Long orgId;
}
