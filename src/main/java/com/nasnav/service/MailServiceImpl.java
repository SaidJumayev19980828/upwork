package com.nasnav.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.nasnav.AppConfig;

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
        sendMessage(asList(to), subject, emptyList(), body);
    }
    
    
    
    
    
    private void sendMessage(List<String> to, String subject, List<String> cc, String body) throws MessagingException {
        if (mailSender == null) {
            return;
        }
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(config.mailSenderAddress);
        helper.setTo(to.toArray(new String[0]));
        helper.setSubject(subject);
        helper.setText(body, true);
        if(nonNull(cc) && !cc.isEmpty()) {
        	helper.setCc(cc.toArray(new String[0]));	
        }
        if (config.mailDryRun) {
            System.out.println("Sending email to: " + to + "\n-------\n" + body);
        } else {
            mailSender.send(mimeMessage);
        }
    }
    
    


    @Override
    public void send(String to, String subject, String template, Map<String, String> parametersMap) throws IOException, MessagingException {
        String body = createBodyFromTemplate(template, parametersMap);
        sendMessage(asList(to), subject, emptyList(), body);
    }
    
    
    
    @Override
    public void send(String to, String subject, List<String> cc, String template, Map<String, String> parametersMap) throws IOException, MessagingException {
        String body = createBodyFromTemplate(template, parametersMap);
        sendMessage(asList(to), subject, cc, body);
    }
    
    
    
    
    @Override
    public void send(List<String> to, String subject, List<String> cc, String template, Map<String, String> parametersMap) throws IOException, MessagingException {
        String body = createBodyFromTemplate(template, parametersMap);
        sendMessage(to, subject, cc, body);
    }





	private String createBodyFromTemplate(String template, Map<String, String> parametersMap) throws IOException {
		Resource resource = new ClassPathResource(template);
        String body = new BufferedReader(new InputStreamReader(resource.getInputStream()))
                			.lines()
                			.collect(joining("\n"));
        for (Map.Entry<String, String> entry : parametersMap.entrySet()) {
            body = body.replaceAll(entry.getKey(), entry.getValue());
        }
		return body;
	}
}
