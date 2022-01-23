package com.nasnav.payments;

import lombok.Getter;

import java.util.Properties;

public class Account  {

    @Getter
    protected String accountId;

    @Getter
    protected int dbId = -1;

    public void setup(Properties properties) {

       this.accountId = properties.getProperty("account.identifier");
    }

}
