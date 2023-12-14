package com.nasnav.service;

import com.nasnav.dto.response.BankAccountDTO;
import com.nasnav.dto.response.BankAccountDetailsDTO;
import com.nasnav.persistence.BankAccountEntity;

public interface BankAccountService {
    public BankAccountDetailsDTO createAccount(BankAccountDTO dto);
    public BankAccountDTO getAccount();
    public Float getOpeningBalance(long accountId);
    public void setOpeningBalance(long accountId);
    public void setAllAccountsOpeningBalance();
    public void lockOrUnlockAccount(long accountId, boolean isLocked);
    public BankAccountDetailsDTO toDto(BankAccountEntity entity);
    public BankAccountEntity getLoggedAccount();
    public BankAccountEntity getAccountByWalletAddress(String walletAddress);
    public Boolean checkAccountExistence(Long accountId);
    BankAccountEntity assignWalletAddress (String walletAddress);

}
