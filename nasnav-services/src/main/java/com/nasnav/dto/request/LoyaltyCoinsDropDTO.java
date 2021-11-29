package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoyaltyCoinsDropDTO {
    private Long id;
    private Integer typeId;
    private Long orgId;
    private Boolean isActive;
    private BigDecimal amount;
    private LocalDate officialVacationDate;
}
