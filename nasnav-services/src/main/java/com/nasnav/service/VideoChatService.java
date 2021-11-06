package com.nasnav.service;

import com.nasnav.response.VideoChatResponse;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface VideoChatService {

    ResponseEntity<JSONObject> removeUser(String userToken, String sessionName);

    VideoChatResponse getSession(String userToken, String sessionName) throws OpenViduHttpException, OpenViduJavaClientException;

    List<String> getAllSessions();

}
