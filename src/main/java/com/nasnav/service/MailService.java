package com.nasnav.service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Map;

public interface MailService {
    /**
     * Send plain Email
     *
     * @param to Email-TO
     * @param subject Email-Subject
     * @param body Email-Body
     * @throws MessagingException If message failed to be sent.
     */
    void send(String to, String subject, String body) throws MessagingException;

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
    void send(String to, String subject, String template, Map<String, String> parametersMap) throws MessagingException, IOException;
}
