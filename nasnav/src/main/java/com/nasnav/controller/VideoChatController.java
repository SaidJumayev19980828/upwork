package com.nasnav.controller;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.VideoChatService;
import io.openvidu.java.client.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/videochat")
@CrossOrigin("*")
public class VideoChatController {

    @Autowired
    private VideoChatService videoChatService;

    @RequestMapping(value = "/getAllSessions", method = RequestMethod.GET)
    public List<String> getAllSessions(@RequestHeader(name = "User-Token", required = false) String userToken) {
      return videoChatService.getAllSessions();
    }

    @GetMapping(value = "/getSession")
    public VideoChatResponse getSession(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam(required = false) String sessionName) throws RuntimeBusinessException, OpenViduJavaClientException, OpenViduHttpException {
        return  videoChatService.getSession(userToken, sessionName);
    }

    @RequestMapping(value = "/remove-user", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> removeUser(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam String sessionName)
            throws Exception {
        return videoChatService.removeUser(userToken, sessionName);
    }
}