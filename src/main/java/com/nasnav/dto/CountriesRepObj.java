package com.nasnav.dto;

import lombok.Data;

import java.util.List;

@Data
public class CountriesRepObj extends BaseRepresentationObject{

    private String name;
    private List<CitiesRepObj> cities;
}
