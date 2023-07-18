package com.nasnav.service;

import com.nasnav.request.VideoChatSearchParam;
import com.nasnav.response.VideoChatListResponse;
import com.nasnav.response.VideoChatResponse;

public interface VideoChatService {

    VideoChatResponse createOrJoinSession(String sessionName, Boolean force, Long orgId, Long shopId);

    VideoChatListResponse getOrgSessions(VideoChatSearchParam params);

    void leaveSession(String sessionName, Long orgId, Long shopId, Boolean endCall);

    void handelCallbackEvent(String eventDTO);
}
