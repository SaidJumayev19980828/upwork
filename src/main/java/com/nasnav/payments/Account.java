package com.nasnav.payments;

import lombok.Getter;

import java.util.Properties;


@Getter
public class Account  {

    protected String merchantId;
    protected String apiVersion;
    protected String apiUsername;
    protected String apiPassword;
    protected String apiUrl;
    protected String identifier;

    protected boolean hasUpg = true;
    protected String upgMerchantId;
    protected String upgTerminalId;
    protected String upgSecureKey;
    protected String upgCallbackUrl;

    private boolean present(String in) {
        return in != null && in.length() > 0;
    }

    public void init(Properties props, String prefix) {
        if (props != null && prefix != null) {
            this.merchantId = props.getProperty(prefix + ".merchant_id");
            this.apiUsername = props.getProperty(prefix + ".api_username");
            this.apiPassword = props.getProperty(prefix + ".api_password");
            this.apiUrl = props.getProperty(prefix + ".api_url");
            this.apiVersion = props.getProperty(prefix + ".api_version");
            this.identifier = props.getProperty(prefix + ".identifier");

            this.upgMerchantId = props.getProperty(prefix + ".upg_mid");
            this.upgTerminalId = props.getProperty(prefix + ".upg_tid");
            this.upgSecureKey = props.getProperty(prefix + ".upg_key");
            this.upgCallbackUrl = props.getProperty(prefix + ".upg_callback");
            if (!present(this.upgMerchantId) || !present(this.upgTerminalId) || !present(this.upgSecureKey)) {
                this.hasUpg = false;
            }
        }
    }
}
