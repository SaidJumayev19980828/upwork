package com.nasnav.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PackageDto {
    private String name;
    private String description;
    private BigDecimal price;
}
