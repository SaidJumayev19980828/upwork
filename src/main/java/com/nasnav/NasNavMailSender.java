package com.nasnav;

import com.nasnav.constatnts.EntityConstants.ConfigurationKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.annotation.PostConstruct;

@PropertySource(value = "classpath:mail.properties")
public class NasNavMailSender extends JavaMailSenderImpl {

    private static final Logger logger = LoggerFactory.getLogger(NasNavMailSender.class);

    @Autowired
    private Environment env;

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
//        setJavaMailProperties(createJavaMailProperties());
        String host = env.getProperty(ConfigurationKey.MAIL_SERVER_HOST_NAME.getValue());
        logger.info("Mail Host : " + host);
        setHost(host);
        Integer port = Integer.parseInt(env.getProperty(ConfigurationKey.MAIL_SERVER_PORT.getValue()));
        logger.info("Mail Port : " + port);
        setPort(port);
        String email = env.getProperty(ConfigurationKey.MAIL_SERVER_EMAIL.getValue());
        logger.info("Email From : " + email);
        setUsername(email);
        String password = env.getProperty(ConfigurationKey.MAIL_SERVER_PASSWORD.getValue());
        logger.info("Email Password : " + password);
        setPassword(password);
    }

    /**
     * Create Properties object to be used for current JavaMailSenderImpl.
     *
     * @return Properties object.
     */
/*
    private Properties createJavaMailProperties() {
        Properties props = new Properties();
        props.put("mail.transport.protocol", configService.getConfigValue(ConfigurationKey.MAIL_SERVER_PROTOCOL, "smtp"));
        props.put("mail.smtp.auth", configService.getConfigValue(ConfigurationKey.MAIL_SERVER_SSL, "true"));
        props.put("mail.smtp.starttls.enable", configService.getConfigValue(ConfigurationKey.MAIL_SERVER_SSL, "true"));
        props.put("mail.smtp.ssl.trust", configService.getConfigValue(ConfigurationKey.MAIL_SERVER_HOST_NAME, "true"));
        return props;
    }
*/
}

