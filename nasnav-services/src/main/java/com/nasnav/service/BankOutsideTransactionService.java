package com.nasnav.service;

public interface BankOutsideTransactionService {
    public void depositOrWithdrawal(long amount, boolean isDeposit, String transactionIdOfBC);
    public Boolean validateDepositOrWithdrawalIsDone(String hashBC, long amount);

}
