package com.nasnav.service.sendpulse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SendPulseService {

    private static final Logger logger = Logger.getLogger(SendPulseService.class.getName());

    private static Sendpulse client;

    public SendPulseService(String userId, String secret) {
        client = new Sendpulse(userId,secret);
    }

    public void smtpSendMail(String fromName, String fromEmail, String nameTo, String emailTo, String html, String subject,
                                    Map<String, String> attachments){
        Map<String, Object> from = new HashMap<>();
        from.put("name", fromName);
        from.put("email", fromEmail);
        ArrayList<Map> to = new ArrayList<>();
        Map<String, Object> elementTo = new HashMap<>();
        elementTo.put("name", nameTo);
        elementTo.put("email", emailTo);
        to.add(elementTo);
        Map<String, Object> emaildata = new HashMap<>();
        emaildata.put("html",html);
        emaildata.put("subject",subject);
        emaildata.put("from",from);
        emaildata.put("to",to);
        if( attachments != null && attachments.size()>0){
            emaildata.put("attachments_binary",attachments);
        }
        Map<String, Object> result =  client.smtpSendMail(emaildata);
        logger.info("Results: " + result);
    }
}
