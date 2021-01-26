package com.nasnav.payments.rave;

import com.nasnav.payments.Account;
import lombok.Getter;

import java.util.Properties;

@Getter
public class RaveAccount extends Account {

    protected String clientName;
    protected String publicKey;
    protected String privateKey;
    protected String encryptionKey;
    protected String successUrl;
    protected String failureUrl;
    protected String scriptUrl;
    protected String apiUrl;
    protected String icon;

    public RaveAccount(Properties props, int dbId) {
        if (props == null) return; // ugly hack, for some deployemnt doesnt see this file ....
            super.setup(props);
            super.accountId = "RAVE:" +super.accountId;
            super.dbId = dbId;

            this.clientName = props.getProperty("rave.client_name");
            this.publicKey = props.getProperty("rave.public_key");
            this.privateKey = props.getProperty("rave.secret_key");
            this.encryptionKey = props.getProperty("rave.enc_key");
            this.successUrl = props.getProperty("rave.success_url");
            this.failureUrl = props.getProperty("rave.failure_url");
            this.apiUrl = props.getProperty("rave.api_url");
            this.scriptUrl = props.getProperty("rave.script_url");
            this.icon = "/icons/rave.svg";
    }
}
