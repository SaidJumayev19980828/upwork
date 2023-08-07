package com.nasnav.service;

import com.nasnav.dto.response.BankActivityDetailsDTO;
import com.nasnav.dto.response.BankBalanceSummaryDTO;
import com.nasnav.persistence.BankAccountActivityEntity;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.persistence.BankInsideTransactionEntity;
import com.nasnav.persistence.BankOutsideTransactionEntity;
import org.springframework.data.domain.PageImpl;

public interface BankAccountActivityService {
    public Long getAvailableBalance(long accountId);
    public Long getTotalBalance(long accountId);
    public Long getReservedBalance(long accountId);
    public Boolean checkAvailableBalance(long accountId, long amount);
    public BankAccountActivityEntity getLastActivity(long accountId);
    public PageImpl<BankActivityDetailsDTO> getHistory(Integer start, Integer count);
    public BankBalanceSummaryDTO getAccountSummary();
    public void addActivity(BankAccountEntity accountEntity, long amount, boolean isDeposit, BankInsideTransactionEntity insideTransactionEntity, BankOutsideTransactionEntity outsideTransactionEntity);
}
