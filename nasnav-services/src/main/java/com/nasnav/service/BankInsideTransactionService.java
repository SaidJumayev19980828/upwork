package com.nasnav.service;

public interface BankInsideTransactionService {
    public void transfer(long receiverAccountId, long amount);
}
