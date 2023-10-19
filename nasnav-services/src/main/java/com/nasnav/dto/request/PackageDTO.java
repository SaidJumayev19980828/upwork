package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PackageDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer currencyIso;
    private Long periodInDays;
    private String stripePriceId;
    private Set<ServiceDTO> services;
    private CountryDTO country;


    @Data
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class CountryDTO{
        private String currency;
    }
}
