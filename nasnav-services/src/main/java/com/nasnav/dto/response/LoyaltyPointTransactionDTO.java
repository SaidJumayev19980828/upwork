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
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long orgId;
    private String type;
    private BigDecimal amount;
    private BigDecimal points;
    private Boolean isValid;
    private Long orderId;
    private Long metaOrderId;
    private Long shopId;
    private String shopName;
    private String shopLogo;
    private Boolean gotOnline;
    private String description;
}
