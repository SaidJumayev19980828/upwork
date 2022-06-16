package com.nasnav.service;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.response.VideoChatResponse;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;

import java.util.List;

public interface VideoChatService {

    VideoChatResponse getSession(String userToken, String sessionName, Long orgId);

    List<BaseRepresentationObject> getOrgSessions(Long orgId);

    void leaveSession(String sessionName, Long orgId);
}
