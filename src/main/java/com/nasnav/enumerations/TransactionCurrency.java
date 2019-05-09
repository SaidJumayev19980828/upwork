package com.nasnav.enumerations;

import lombok.Getter;

public enum TransactionCurrency {
    UNSPECIFIED(-1), EGP(1), USD(2);

    @Getter
    private int value;

    TransactionCurrency(int value) {
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