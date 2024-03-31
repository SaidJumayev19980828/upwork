package com.nasnav.dto.request.product;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ThreeDModelDTO {
    @JsonIgnore
    private Long id;
    private String name;
    private String description;
    private String barcode;
    private String sku;
    private String color;
    private String model;
    private Long size;
}
