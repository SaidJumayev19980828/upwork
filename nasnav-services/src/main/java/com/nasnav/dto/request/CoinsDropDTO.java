package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CoinsDropDTO {
    private Long id;
    private Integer typeId;
    private Long orgId;
    private Boolean isActive;
    private Integer amount;
    private LocalDate officialVacationDate;
}
