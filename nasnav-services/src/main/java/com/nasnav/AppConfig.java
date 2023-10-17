package com.nasnav;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:application.properties")
public class AppConfig {
    @Value("${email.dryrun}")      		public boolean mailDryRun;
    @Value("${email.hostname}")    		public String mailHostname;
    @Value("${email.mailfrom}")    		public String mailSenderAddress;
    @Value("${email.username}")    		public String mailUsername;
    @Value("${email.password}")    		public String mailPassword;
    @Value("${email.recoveryurl}") 		public String mailRecoveryUrl = "";
    @Value("${email.activationurl}")  	public String accountActivationUrl;
    @Value("${email.port}")        		public int    mailHostPort;
    @Value("${email.ssl}")         		public String mailUseSSL;
    @Value("${payment.properties_dir}") public String paymentPropertiesDir = "";
    @Value("${yeshtery.mastercard}")    public String yeshteryMastercardProperties = "";
    @Value("${yeshtery.orgid:0}")
    public int yeshteryOrgId;
    @Value("${nasnav.orgid:0}")
    public long nasnavOrgId;
    @Value("${cookies.secureToken}")    public boolean secureTokens;
    @Value("${dashbaord.order.page}")  	public String dashBoardOrderPageUrl;
    @Value("${environment.development}") public boolean develEnvironment = false;
    @Value("${environment.hostname}")   public String environmentHostName;
    @Value("${email.url.emp_recover}")  public String empMailRecoveryUrl = "";
    
    @Value("${openvidu_url}") public String openViduUrl = "";
    @Value("${openvidu_secret}") public String openViduSecret = "";
    @Value("${otp.valid-duration-in-seconds:600}")
    public int otpValidDurationInSeconds;
    @Value("${otp.length:6}")
    public int otpLength;
    @Value("${otp.max-retries:3}")
    public int otpMaxRetries;
    @Value("${rocketchat.url:https://chat.dev.meetusvr.com/api/v1}")
    public String rocketChatUrl;
    @Value("${firebase-config:}")        public String firebaseConfig;

    @Value("${files.basepath}")
    @Getter
    @Setter
    private String basePathStr;

    @Value("${stripe.apikey:sk_test_51NxqlfGR4qGEOW4E6Qni6REIWcwheVdU8mf2LtTVn1BWn8dtdQSg7stf9b0cqE8CJZVja9aTuOISKg15qC52CjLf00bmLe17sU}")
    public String stripeApiKey;
    @Value("${stripe.webhook.secret:whsec_c6c1772b65026654a21e1beac00f0a213eacbb56edd1bca45a8fdfd10fdb1c6c}")
    public String stripeWebhookSecret;
    public final boolean isYeshteryInstance;
    public AppConfig(boolean isYeshtery) {
        isYeshteryInstance = isYeshtery;
    }
}
