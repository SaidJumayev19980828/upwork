package com.nasnav.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class PackageDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer currencyIso;
    private Long periodInDays;
    private Set<ServiceDTO> services;
}
