package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CountryDTO {
    private Long id;
    private String name;
    @JsonProperty("iso_code")
    private Integer isoCode;
    private String currency;
    private List<CityDTO> cities;
}
