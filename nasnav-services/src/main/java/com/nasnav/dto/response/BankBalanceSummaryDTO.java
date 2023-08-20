package com.nasnav.dto.response;

import lombok.Data;

@Data
public class BankBalanceSummaryDTO {
    private Float totalBalance;
    private Float reservedBalance;
    private Float availableBalance;
}
