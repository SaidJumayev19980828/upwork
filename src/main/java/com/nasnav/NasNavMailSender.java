package com.nasnav;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.annotation.PostConstruct;
import java.util.Properties;

@PropertySource(value = "classpath:mail.properties")
public class NasNavMailSender extends JavaMailSenderImpl {

    private static final Logger logger = LoggerFactory.getLogger(NasNavMailSender.class);

    @Autowired
    private AppConfig config;

    public boolean dryRun = false;
    public String senderAddress = "";

    public NasNavMailSender() {
        setHost("");
        setPort(0);
        setUsername("");
        setPassword("");
    }

    /**
     * Setup mail host that will be used to send emails from app.
     *
     */
    @PostConstruct
    public void init() {
        logger.info("DryRun : " + dryRun);
        dryRun = config.mailDryRun;
        logger.info("Mail Host : " + config.mailHostname + ":" + config.mailHostPort);
        setHost(config.mailHostname);
        setPort(config.mailHostPort);
        if (config.mailUsername != null && config.mailUsername != "") {
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", true);
            props.put("mail.smtp.starttls.enable", config.mailUseSSL);
            props.put("mail.smtp.ssl.trust", true);
            setUsername(config.mailUsername);
            setPassword(config.mailPassword);
        }
        this.senderAddress = config.mailSenderAddress;
    }
}

