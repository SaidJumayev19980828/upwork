package com.nasnav.dto.response;

import lombok.Data;

@Data
public class BankBalanceSummaryDTO {
    private Long totalBalance;
    private Long reservedBalance;
    private Long availableBalance;
}
