package com.nasnav.service;

import com.nasnav.dto.VideoChatLogRepresentationObject;
import com.nasnav.request.VideoChatSearchParam;
import com.nasnav.response.VideoChatListResponse;
import com.nasnav.response.VideoChatResponse;

import java.util.List;

public interface VideoChatService {

    VideoChatResponse createOrJoinSession(String sessionName, Long orgId, Long shopId);

    VideoChatListResponse getOrgSessions(VideoChatSearchParam params);

    void leaveSession(String sessionName, Long orgId, Long shopId, Boolean endCall);
}
