package com.nasnav.dto;

import lombok.Data;

import java.util.List;

@Data
public class CityDTO {
    private Long id;
    private String name;
    private List<AreaDTO> areas;
}
