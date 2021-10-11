package com.nasnav.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoyaltyPointTransactionDTO {

    private Long id;
    private Integer points;
    private String code;
}
