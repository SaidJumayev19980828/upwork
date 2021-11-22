package com.nasnav.dto.request;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoyaltyEventDTO {
    private Long id;
    private String name;
    private Long organizationId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;

}
