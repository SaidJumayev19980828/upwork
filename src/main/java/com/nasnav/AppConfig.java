package com.nasnav;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    @Value("${paymnet.properties_dir}") public String paymentPropertiesDir = "";
    @Value("${cookies.secureToken}")    public boolean secureTokens;
    @Value("${dashbaord.order.page}")  	public String dashBoardOrderPageUrl;
    

    
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    
    
    @Bean
    public JavaMailSender mailSender() {
        return new NasNavMailSender();
    }

}
