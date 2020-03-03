package com.nasnav.payments.mastercard;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ActiveSessions {

    private HashMap<String, MastercardSession> sessions = new HashMap<>();

    public void add(MastercardSession session) {
        sessions.put(session.getSessionId(), session);
    }

//    public Session getById(String sessionId) {
//        return sessions.get(sessionId);
//    }

    // TODO: cleanup old sessions from the table (do a check on every add call)?

}
