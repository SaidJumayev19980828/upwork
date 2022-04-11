package com.nasnav.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
public class LoyaltyPointTransactionDTO {

    private Long orgId;
    private Long typeId;
    private String type;
    private BigDecimal amount;
    private BigDecimal points;
    private Boolean isValid;
    private Long orderId;
    private Long shopId;

}
