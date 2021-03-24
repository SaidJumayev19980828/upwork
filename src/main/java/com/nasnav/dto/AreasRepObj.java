package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AreasRepObj extends BaseRepresentationObject{

    private Long id;
    private String name;
    private Map<String, SubAreasRepObj> subAreas;
}
