package com.nasnav.payments;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class Account  {

    @Getter
    protected String accountId;

    @Getter
    protected int dbId = -1;

    private static final Logger tempLogger = LogManager.getLogger("Payment:MCARD");

    public void setup(Properties properties) {

       tempLogger.debug("Properties REF2 {}", properties);
       tempLogger.debug("Acc ID", properties.getProperty("account.identifier"));
       this.accountId = properties.getProperty("account.identifier");
    }

}
