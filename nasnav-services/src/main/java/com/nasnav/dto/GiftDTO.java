package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GiftDTO {
    private Long id;
    private Long userFromId;
    private Long userToId;
    private Boolean isActive;
    private BigDecimal points;
    private String phoneNumber;
    private String email;
    private Boolean isRedeem;
}
