package com.nasnav.payments.mastercard;

import com.nasnav.payments.Account;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Getter
public class MastercardAccount extends Account {

    protected String merchantId;
//    protected String apiVersion;
    protected String apiUsername;
    protected String apiPassword;
    protected String apiUrl;
    protected String scriptUrl;
    protected String icon;
    protected int flavor = MastercardService.FLAVOR_BASIC;

    private static final Logger tempLogger = LogManager.getLogger("Payment:MCARD");

    public synchronized void init(Properties props, int dbId) {

        tempLogger.debug("Properties REF2 {}", props);
        if (props == null) {
            return;
        }
        tempLogger.debug("Acc ID {}", props.getProperty("account.identifier"));
//        super.setup(props);
        this.merchantId = props.getProperty("mcard.merchant_id");
        this.apiUsername = props.getProperty("mcard.api_username");
        this.apiPassword = props.getProperty("mcard.api_password");
        this.apiUrl = props.getProperty("mcard.api_url");
//        this.apiVersion = props.getProperty("mcard.api_version");
        this.scriptUrl = props.getProperty("mcard.script_url");
        super.accountId = "MCARD:" +super.accountId;
        super.dbId = dbId;
        this.icon = "/icons/mastercard.svg";
        try {
            this.flavor = Integer.parseInt(props.getProperty("mcard.flavor"));
        } catch (Exception ex) {  }
        if (this.flavor <= 0) {
            this.flavor = MastercardService.FLAVOR_BASIC;
        }
    }
}
