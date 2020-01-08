package com.nasnav.payments;

import lombok.Getter;

import java.util.Properties;


@Getter
//@Setter
public class Account  {

    protected String merchantId;

    protected String apiVersion;

    protected String apiUsername;

    protected String apiPassword;

    protected String apiUrl;

    protected String identifier;

    public void init(Properties props, String prefix) {
        if (props != null && prefix != null) {
            this.merchantId = props.getProperty(prefix + ".merchant_id");
            this.apiUsername = props.getProperty(prefix + ".api_username");
            this.apiPassword = props.getProperty(prefix + ".api_password");
            this.apiUrl = props.getProperty(prefix + ".api_url");
            this.apiVersion = props.getProperty(prefix + ".api_version");
            this.identifier = props.getProperty(prefix + ".identifier");

//System.out.println("prefix: " + prefix + " : " + ".merchant_id : " + this.merchantId);
        }
    }

}
