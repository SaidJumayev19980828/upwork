package com.nasnav.payments.qnb;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@PropertySource(value = "classpath:provider.qnb.properties")
@ConfigurationProperties(prefix = "qnb")
@Getter
@Setter
public class Account {

    @PostConstruct
    void init() {
        // ugly hack, but for some reason the bin gets re-instantiated and overwritten with null
        if (merchantId != null && safeMerchantId == null) {
            safeMerchantId = merchantId;
            safeApiUrl = apiUrl;
            safeApiUsername = apiUsername;
            safeApiPassword = apiPassword;
            safeApiVersion = apiVersion;
            safeUpgMerchantId = upgMerchantId;
            safeUpgTerminalId = upgTerminalId;
            safeUpgSecureKey = upgSecureKey;
            safeUpgCallbackUrl = upgCallbackUrl;
        }
    }

    @Value("${qnb.merchant_id}")
    private String merchantId;
    private static String safeMerchantId;

    @Value("${qnb.upg.mid}")
    private String upgMerchantId;
    private static String safeUpgMerchantId;

    @Value("${qnb.upg.tid}")
    private String upgTerminalId;
    private static String safeUpgTerminalId;

    @Value("${qnb.upg.key}")
    private String upgSecureKey;
    private static String safeUpgSecureKey;

    @Value("${qnb.upg.callback}")
    private String upgCallbackUrl;
    private static String safeUpgCallbackUrl;

    @Value("${qnb.api_version}")
    private String apiVersion;
    private static String safeApiVersion;

    @Value("${qnb.api_username}")
    private String apiUsername;
    private static String safeApiUsername;

    @Value("${qnb.api_password}")
    private String apiPassword;
    private static String safeApiPassword;

    @Value("${qnb.api_url}")
    private String apiUrl;
    private static String safeApiUrl;

        public String getMerchantId() {
        return safeMerchantId;
    }

    public String getApiUsername() {
        return safeApiUsername;
    }

    public String getApiPassword() {
        return safeApiPassword;
    }

    public String getApiUrl() {
        return safeApiUrl;
    }

    public String getApiVersion() {
        return safeApiVersion;
    }

    public String getUpgMerchantId() { return safeUpgMerchantId; }

    public String getUpgTerminalId() { return safeUpgTerminalId; }

    public String getUpgSecureKey() { return safeUpgSecureKey; }

    public String getUpgCallbackUrl() { return safeUpgCallbackUrl; }
}
