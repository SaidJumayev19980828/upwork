package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PackageDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer currencyIso;
    private Long periodInDays;
    private String stripePriceId;
    private List<Long> serviceIds;
}
