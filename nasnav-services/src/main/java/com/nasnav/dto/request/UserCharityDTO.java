package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserCharityDTO {
    private Long id;
    private Integer donationPercentage;
    private Long charityId;
    private Boolean isActive;
    private Long userId;
}
