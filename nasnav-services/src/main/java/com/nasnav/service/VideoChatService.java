package com.nasnav.service;

import com.nasnav.dto.VideoChatLogRepresentationObject;
import com.nasnav.persistence.UserEntity;
import com.nasnav.request.VideoChatSearchParam;
import com.nasnav.response.VideoChatListResponse;
import com.nasnav.response.VideoChatResponse;

import java.util.List;

public interface VideoChatService {

    VideoChatResponse createOrJoinSession(String sessionName, Boolean force, Long orgId, Long shopId);

    VideoChatResponse createOrJoinSessionForUser(String sessionName, Boolean force, Long orgId, Long shopId, UserEntity user);

    VideoChatListResponse getOrgSessions(VideoChatSearchParam params);

    void leaveSession(String sessionName, Long orgId, Long shopId, Boolean endCall);

    void handelCallbackEvent(String eventDTO);
}
