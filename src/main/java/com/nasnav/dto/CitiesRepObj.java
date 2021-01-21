package com.nasnav.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;


@Data
@EqualsAndHashCode(callSuper = true)
public class CitiesRepObj extends BaseRepresentationObject{

    private Long id;
    private String name;
    private Map<String, AreasRepObj> areas;
}
