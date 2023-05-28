package com.nasnav.service;

public interface BankOutsideTransactionService {
    public void depositOrWithdrawal(long amount, boolean isDeposit, long transactionIdOfBC);
    public Boolean validateDepositOrWithdrawalIsDone(long depositOrWithdrawalId);
    //get history
}
