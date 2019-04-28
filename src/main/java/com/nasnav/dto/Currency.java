package com.nasnav.dto;

import lombok.Getter;

public enum Currency{
    EGP(1);

    @Getter
    private int currencyId;

    Currency(int currencyId){
        this.currencyId = currencyId;
    }

    public static Currency findById(Integer id){
        for (Currency currency : Currency.values()){
            if(currency.getCurrencyId() == id){
                return currency;
            }
        }
        return null;
    }
}
