package com.nasnav.service;

public interface BankOutsideTransactionService {
    public void depositOrWithdrawal(float amount, boolean isDeposit, String transactionIdOfBC);
    public Boolean validateDepositOrWithdrawalIsDone(String hashBC, float amount);

}
