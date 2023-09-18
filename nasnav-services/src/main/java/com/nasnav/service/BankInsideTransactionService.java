package com.nasnav.service;

import com.nasnav.persistence.BankAccountEntity;

public interface BankInsideTransactionService {
    public void transfer(long receiverAccountId, float amount);
    public void pay(float amount);
    public void transferImpl(BankAccountEntity sender, BankAccountEntity receiver, float amount);
}
