package com.nasnav.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BankActivityDTO extends BankAccountDTO{
    private List<BankActivityDetailsDTO> history;
    private BankBalanceSummary summary;
}
