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
        }
    }

    @Value("${qnb.merchant_id}")
    private String merchantId;
    private static String safeMerchantId;

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

}
