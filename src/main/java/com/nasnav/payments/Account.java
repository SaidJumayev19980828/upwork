package com.nasnav.payments;

import lombok.Getter;

import java.io.InputStream;
import java.util.Properties;

public class Account  {

    @Getter
    protected String accountId;

    public void setup(Properties properties) {
       this.accountId = properties.getProperty("account.identifier");
    }

}
