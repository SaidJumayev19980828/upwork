package com.nasnav.service;

import com.nasnav.dto.BankAccountDTO;

public interface BankAccountService {
    public BankAccountDTO createAccount(BankAccountDTO dto);
    public Long getOpeningBalance(long accountId);
    public void updateOpeningBalance(long accountId, long newBalance);
    public void lockOrUnlockAccount(long accountId, boolean isLocked);
}
