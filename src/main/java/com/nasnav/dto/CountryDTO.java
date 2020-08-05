package com.nasnav.dto;

import lombok.Data;

import java.util.List;

@Data
public class CountryDTO {
    private Long id;
    private String name;
    private List<CityDTO> cities;
}
