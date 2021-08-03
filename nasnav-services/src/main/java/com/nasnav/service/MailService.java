package com.nasnav.service;

import com.nasnav.service.model.mail.MailAttachment;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MailService {

    /**
     * Send Email using the passed html template.
     * @param to Email-TO
     * @param subject Email-Subject
     * @param template name of html template to be used as Email body.
     * @param parametersMap Used to replace static parameters of template with the
     *                      values of each parameter in map.
     * @throws MessagingException
     * @throws IOException
     */
    void send(String org, String to, String subject, String template, Map<String, String> parametersMap) throws MessagingException, IOException;

	void sendThymeleafTemplateMail(String org, List<String> to, String subject, List<String> cc, String template,
			Map<String, Object> parametersMap) throws IOException, MessagingException;
	
	void sendThymeleafTemplateMail(String org, List<String> to, String subject, List<String> cc, List<String> bcc,String template,
			Map<String, Object> parametersMap) throws IOException, MessagingException;

	void sendThymeleafTemplateMail(String org, String string, String subject, String template, Map<String, Object> parametersMap) throws MessagingException;

	void sendThymeleafTemplateMail(String org, String string, String subject
			, String template, Map<String, Object> parametersMap, List<MailAttachment> attachments) throws MessagingException;

	String createBodyFromThymeleafTemplate(String template, Map<String,Object> variables);
}
