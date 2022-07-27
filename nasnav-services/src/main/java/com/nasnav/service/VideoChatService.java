package com.nasnav.service;

import com.nasnav.dto.VideoChatLogRepresentationObject;
import com.nasnav.response.VideoChatResponse;

import java.util.List;

public interface VideoChatService {

    VideoChatResponse createOrJoinSession(String sessionName, Long orgId);

    List<VideoChatLogRepresentationObject> getOrgSessions(Long orgId);

    void leaveSession(String sessionName, Long orgId, Boolean endCall);
}
