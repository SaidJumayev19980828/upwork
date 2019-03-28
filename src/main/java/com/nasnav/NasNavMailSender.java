package com.nasnav;

import com.nasnav.constatnts.EntityConstants.ConfigurationKey;
import com.nasnav.service.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.annotation.PostConstruct;
import java.util.Properties;

public class NasNavMailSender extends JavaMailSenderImpl {

    private static final Logger logger = LoggerFactory.getLogger(NasNavMailSender.class);

    @Autowired
    private ConfigurationService configService;

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
        setJavaMailProperties(createJavaMailProperties());
        String host = configService.getConfigValue(ConfigurationKey.MAIL_SERVER_HOST_NAME, null);
        logger.info("Mail Host : " + host);
        setHost(host);
        Integer port = configService.getConfigIntValue(ConfigurationKey.MAIL_SERVER_PORT, 0);
        logger.info("Mail Port : " + port);
        setPort(port);
        String email = configService.getConfigValue(ConfigurationKey.MAIL_SERVER_EMAIL, null);
        logger.info("Email From : " + email);
        setUsername(email);
        String password = configService.getConfigValue(ConfigurationKey.MAIL_SERVER_PASSWORD, null);
        logger.info("Email Password : " + password);
        setPassword(password);
    }

    /**
     * Create Properties object to be used for current JavaMailSenderImpl.
     *
     * @return Properties object.
     */
    private Properties createJavaMailProperties() {
        Properties props = new Properties();
        props.put("mail.transport.protocol", configService.getConfigValue(ConfigurationKey.MAIL_SERVER_PROTOCOL, "smtp"));
        props.put("mail.smtp.auth", configService.getConfigValue(ConfigurationKey.MAIL_SERVER_SSL, "true"));
        props.put("mail.smtp.starttls.enable", configService.getConfigValue(ConfigurationKey.MAIL_SERVER_SSL, "true"));
        props.put("mail.smtp.ssl.trust", configService.getConfigValue(ConfigurationKey.MAIL_SERVER_HOST_NAME, "true"));
        return props;
    }
}

