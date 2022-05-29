package com.nasnav;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
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
    @Value("${yeshtery.orgid}")         public int yeshteryOrgId = 0;
    @Value("${cookies.secureToken}")    public boolean secureTokens;
    @Value("${dashbaord.order.page}")  	public String dashBoardOrderPageUrl;
    @Value("${environment.development}") public boolean develEnvironment = false;
    @Value("${environment.hostname}")   public String environmentHostName;
    @Value("${email.url.emp_recover}")  public String empMailRecoveryUrl = "";

    @Value("${openvidu.url}") public String openViduUrl = "";
    @Value("${openvidu.secret}") public String openViduSecret = "";


    @Value("${files.basepath}")
    @Getter
    @Setter
    private String basePathStr;
}
