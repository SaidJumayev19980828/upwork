package com.nasnav.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BankAccountDetailsDTO extends BankAccountDTO{
    private Float openingBalance;
    private LocalDateTime openingBalanceDate;
    private Boolean locked;
    private BankBalanceSummaryDTO summary;
}
