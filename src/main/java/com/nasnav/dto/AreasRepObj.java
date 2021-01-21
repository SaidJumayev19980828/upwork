package com.nasnav.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AreasRepObj extends BaseRepresentationObject{

    private Long id;
    private String name;
    private Map<String, SubAreasRepObj> subAreas;
}
