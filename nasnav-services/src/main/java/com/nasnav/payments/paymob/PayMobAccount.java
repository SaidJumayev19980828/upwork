package com.nasnav.payments.paymob;

import com.nasnav.payments.Account;
import lombok.Getter;
import lombok.NonNull;

import java.util.Properties;

@Getter
public class PayMobAccount extends Account {

    private String apiUrl;
    private String publicKey;
    private String privateKey;

    private String authTokenUrl;
    private String orderUrl;
    private String paymentKeyUrl;
    protected String icon;

    public PayMobAccount(@NonNull Properties props, int dbId) {
        if (props == null) {
            return;
        }
        setup(props);
        super.accountId = "PayMob";
        super.dbId = dbId;
        this.apiUrl = props.getProperty("paymob.api_url");
        this.publicKey = props.getProperty("paymob.public_key");
        this.privateKey = props.getProperty("paymob.secret_key");

        this.authTokenUrl = this.apiUrl + "/auth/tokens";
        this.orderUrl = this.apiUrl + "/ecommerce/orders";
        this.paymentKeyUrl = this.apiUrl + "/acceptance/payment_keys";

        this.icon = "/icons/paymob.svg";

    }


}
