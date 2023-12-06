package com.nasnav.service;

public interface BankOutsideTransactionService {
    public void depositOrWithdrawal(float amount, boolean isDeposit, String transactionIdOfBC);

    void depositCoins(float amount);
    public Boolean validateDepositOrWithdrawalIsDone(String hashBC, float amount);

}
