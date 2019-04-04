package com.nasnav.service;

import com.nasnav.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    private AppConfig config;


    @Override
    public void send(String to, String subject, String body) throws MessagingException {
        sendMessage(to, subject, body);
    }

    /**
     * Used to send an Email.
     *
     * @param to Email-TO
     * @param subject Email-Subject
     * @param body Email-Body
     * @throws MessagingException If message failed to be sent.
     */
    private void sendMessage(String to, String subject, String body) throws MessagingException {
        if (mailSender == null) {
            return;
        }
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(config.mailSenderAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        if (config.mailDryRun) {
            System.out.println("Sending email to: " + to + "\n-------\n" + body);
        } else {
            mailSender.send(mimeMessage);
        }
    }


    @Override
    public void send(String to, String subject, String template, Map<String, String> parametersMap) throws IOException, MessagingException {
        Resource resource = new ClassPathResource(template);
        String body = new BufferedReader(new InputStreamReader(resource.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        for (Map.Entry<String, String> entry : parametersMap.entrySet()) {
            body = body.replaceAll(entry.getKey(), entry.getValue());
        }
        sendMessage(to, subject, body);

    }
}
