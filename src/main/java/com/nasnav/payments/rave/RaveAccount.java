package com.nasnav.payments.rave;

import com.nasnav.payments.Account;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Properties;

@Getter
public class RaveAccount extends Account {

    protected String clientName;
    protected String publicKey;
    protected String privateKey;
    protected String encryptionKey;
    protected String successUrl;
    protected String failureUrl;


    public RaveAccount(Properties props) {
            super.setup(props);
            this.clientName = props.getProperty("rave.client_name");
            this.publicKey = props.getProperty("rave.public_key");
            this.privateKey = props.getProperty("rave.secret_key");
            this.encryptionKey = props.getProperty("rave.enc_key");
            this.successUrl = props.getProperty("rave.success_url");
            this.failureUrl = props.getProperty("rave.failure_url");
    }
}
