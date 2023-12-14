package com.nasnav.service;

import com.nasnav.dto.response.DepositBlockChainRequest;

public interface BankOutsideTransactionService {
    public void depositOrWithdrawal(float amount, boolean isDeposit, String transactionIdOfBC);

    void depositCoins(String amount);
    public Boolean validateDepositOrWithdrawalIsDone(String hashBC, float amount);
    void depositCoinsFromBC(DepositBlockChainRequest request);


}
