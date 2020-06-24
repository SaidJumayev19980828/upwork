package com.nasnav.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CountriesRepObj extends BaseRepresentationObject{
    private Long id;
    private String name;
    private Map cities;
}
