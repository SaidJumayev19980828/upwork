package com.nasnav.payments.upg;

import com.nasnav.payments.Account;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
//@PropertySource(value = "classpath:provider.qnb.properties")
//@ConfigurationProperties(prefix = "upg")
@Getter
public class UpgAccount extends Account {

    protected boolean hasUpg = true;
    protected String upgMerchantId;
    protected String upgTerminalId;
    protected String upgSecureKey;
    protected String upgScriptUrl;
//    protected String upgCallbackUrl;
    protected String icon;

    public void init(@NonNull Properties props) {
        super.setup(props);
        this.upgMerchantId = props.getProperty("upg.mid");
        this.upgTerminalId = props.getProperty("upg.tid");
        this.upgSecureKey = props.getProperty("upg.key");
//        this.upgCallbackUrl = props.getProperty("upg.callback");
        super.accountId = "UPG:" +super.accountId;
        this.upgScriptUrl = props.getProperty("upg.script_url");
        this.icon = "/icons/meeza.svg";
    }
    
}
