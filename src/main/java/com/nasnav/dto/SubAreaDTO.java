package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SubAreaDTO {
    private Long id;
    private String name;
    private Long areaId;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
