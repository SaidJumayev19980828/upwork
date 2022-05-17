package com.nasnav.controller;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.VideoChatService;
import io.openvidu.java.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/videochat")
@CrossOrigin("*")
public class VideoChatController {

    @Autowired
    private VideoChatService videoChatService;

    @RequestMapping(value = "/get-sessions", method = RequestMethod.GET)
    public List<BaseRepresentationObject> getOrgSessions(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                         @RequestParam(name = "org_id") Long orgId) {
      return videoChatService.getOrgSessions(orgId);
    }

    @GetMapping(value = "/get-session")
    public VideoChatResponse getSession(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam(required = false) String sessionName,
                                        @RequestParam(name = "org_id") Long orgId) throws RuntimeBusinessException, OpenViduJavaClientException, OpenViduHttpException {
        return  videoChatService.getSession(userToken, sessionName, orgId);
    }

    @RequestMapping(value = "/leave", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveSession(@RequestHeader(name = "User-Token" , required = true) String userToken, @RequestParam String sessionName, @RequestParam Long orgId) {
        videoChatService.leaveSession(sessionName, orgId);
    }
}