package com.nasnav.enumerations;

import lombok.Getter;

public enum TransactionCurrency {
    EGP(0),USD(1);

    @Getter
    private int value;
    private TransactionCurrency(int value) {
        this.value = value;
    }
    public static TransactionCurrency getTransactionCurrency(int value) {

        for(TransactionCurrency transactionCurrency : TransactionCurrency.values()) {
            if(transactionCurrency.value==value)
                return transactionCurrency;
        }
        return null;
    }
};