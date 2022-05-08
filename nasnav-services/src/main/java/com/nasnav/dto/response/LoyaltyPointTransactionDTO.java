package com.nasnav.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoyaltyPointTransactionDTO {

    private Long id;
    private LocalDateTime createdAt;
    private Long orgId;
    private Long typeId;
    private String type;
    private BigDecimal amount;
    private BigDecimal points;
    private Boolean isValid;
    private Long orderId;
    private Long shopId;
    private Boolean isCoinsDrop;
    private Boolean isGift;
    private Boolean gotOnline;
}
