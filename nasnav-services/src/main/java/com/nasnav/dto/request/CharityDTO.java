package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CharityDTO {
    private Long id;
    private Integer totalDonation;
    private Long orgId;
    private Boolean isActive;
    private String charityName;
}
