package com.nasnav.yeshtery;

import com.nasnav.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
@PropertySource(value = "classpath:mail.properties")
public class YeshteryMailSender extends JavaMailSenderImpl {

    private static final Logger logger = LoggerFactory.getLogger(YeshteryMailSender.class);

    @Autowired
    private AppConfig config;

    private boolean dryRun = false;
    private String senderAddress = "";

    public YeshteryMailSender() {
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
        dryRun = config.mailDryRun;
        logger.info("DryRun : " + dryRun);
        logger.info("Mail Host : " + config.mailHostname + ":" + config.mailHostPort);
        setHost(config.mailHostname);
        setPort(config.mailHostPort);
        if (config.mailUsername != null && !config.mailUsername.equals("")) {
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", config.mailUseSSL);
//            props.put("mail.smtp.ssl.trust", "true");
//            props.put("mail.debug", "true");
            setJavaMailProperties(props);

            setUsername(config.mailUsername);
            setPassword(config.mailPassword);
        }


        this.senderAddress = config.mailSenderAddress;
    }
}

