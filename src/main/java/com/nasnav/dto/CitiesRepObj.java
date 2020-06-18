package com.nasnav.dto;

import lombok.Data;

import java.util.List;

@Data
public class CitiesRepObj extends BaseRepresentationObject{

    private String name;
    private List<AreasRepObj> areas;
}
