package com.nasnav.service.sendpulse;

import java.util.Map;

// rest of the methods removed as they are unneeded and un tested
// can be found at in https://github.com/sendpulse/sendpulse-rest-api-java
public interface SendpulseInterface {
    public Map<String, Object> smtpSendMail( Map<String, Object> emailData );
}