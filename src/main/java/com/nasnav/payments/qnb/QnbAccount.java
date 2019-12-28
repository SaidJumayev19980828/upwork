package com.nasnav.payments.qnb;

import com.nasnav.payments.Account;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Properties;

@Component
@PropertySource(value = "classpath:provider.qnb.properties")
@ConfigurationProperties(prefix = "upg")
@Getter
//@Setter
public class QnbAccount extends Account {

    @PostConstruct
    public void setup() {
        Properties properties = new Properties();
        try (final InputStream stream =
                     this.getClass().getResourceAsStream("/provider.qnb.properties")) {
            properties.load(stream);
            super.init(properties, "qnb");
        } catch (Exception ex) {
            System.err.println("Unable to load resource: provider.qnb.properties");
            ex.printStackTrace();
        }

        // ugly hack, but for some reason the bin gets re-instantiated and overwritten with null
        if (upgMerchantId != null && safeUpgMerchantId == null) {
            safeUpgMerchantId = upgMerchantId;
            safeUpgTerminalId = upgTerminalId;
            safeUpgSecureKey = upgSecureKey;
            safeUpgCallbackUrl = upgCallbackUrl;
        }
    }

    @Value("${upg.mid}")
    private String upgMerchantId;
    private static String safeUpgMerchantId;

    @Value("${upg.tid}")
    private String upgTerminalId;
    private static String safeUpgTerminalId;

    @Value("${upg.key}")
    private String upgSecureKey;
    private static String safeUpgSecureKey;

    @Value("${upg.callback}")
    private String upgCallbackUrl;
    private static String safeUpgCallbackUrl;

    public String getUpgMerchantId() { return safeUpgMerchantId; }

    public String getUpgTerminalId() { return safeUpgTerminalId; }

    public String getUpgSecureKey() { return safeUpgSecureKey; }

    public String getUpgCallbackUrl() { return safeUpgCallbackUrl; }
}
