package com.nasnav.service;

public interface BankInsideTransactionService {
    public void transfer(long receiverAccountId, float amount);
    // overload para: userId
}
