package com.nasnav.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

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
    
    
    private  SpringTemplateEngine templateEngine;
    
    
    @PostConstruct
    public void init() {
        templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlTemplateResolver());
    }
    
    
    

    @Override
    public void send(String to, String subject, String body) throws MessagingException {
        sendMessage(asList(to), subject, emptyList(), body);
    }
    
    
    
    
    
    private void sendMessage(List<String> to, String subject, List<String> cc, String body) throws MessagingException {
    	sendMessage(to, subject, cc, emptyList(), body);
    }
    
    
    
    
    
    private void sendMessage(List<String> to, String subject, List<String> cc, List<String> bcc,String body) throws MessagingException {
        if (mailSender == null) {
            return;
        }
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(config.mailSenderAddress);
        helper.setTo(to.toArray(new String[0]));
        helper.setSubject(subject);
        helper.setText(body, true);
        if(nonNull(cc) && !cc.isEmpty()) {
        	helper.setCc(cc.toArray(new String[0]));	
        }
        if(nonNull(bcc) && !bcc.isEmpty()) {
        	helper.setBcc(bcc.toArray(new String[0]));	
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
    
    
    
    
    @Override
    public void sendThymeleafTemplateMail(List<String> to, String subject, List<String> cc, String template, Map<String, Object> parametersMap) throws IOException, MessagingException {
        String body = createBodyFromThymeleafTemplate(template, parametersMap);
        sendMessage(to, subject, cc, body);
    }
    
    
    
    
    @Override
	public void sendThymeleafTemplateMail(String to, String subject, String template,
			Map<String, Object> parametersMap) throws MessagingException {
    	String body = createBodyFromThymeleafTemplate(template, parametersMap);
    	 sendMessage(asList(to), subject, emptyList(), body);
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
	
	
	
	
	private String createBodyFromThymeleafTemplate(String template, Map<String,Object> variables) {
		Context ctx = new Context(getLocale());
		ctx.setVariables(variables);
		return this.templateEngine.process(template, ctx);
	}
	
	
	
	
	private ITemplateResolver htmlTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
//        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setOrder(Integer.valueOf(1));
//        templateResolver.setResolvablePatterns(singleton("html/*"));
        templateResolver.setPrefix("/mail_templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(true);
        return templateResolver;
    }




	@Override
	public void sendThymeleafTemplateMail(List<String> to, String subject, List<String> cc, List<String> bcc,
			String template, Map<String, Object> parametersMap) throws IOException, MessagingException {
		 String body = createBodyFromThymeleafTemplate(template, parametersMap);
	     sendMessage(to, subject, cc, bcc, body);
	}
}
