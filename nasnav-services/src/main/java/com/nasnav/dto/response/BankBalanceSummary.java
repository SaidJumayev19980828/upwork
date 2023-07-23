package com.nasnav.dto.response;

import lombok.Data;

@Data
public class BankBalanceSummary {
    private Long totalBalance;
    private Long reservedBalance;
    private Long availableBalance;
}
