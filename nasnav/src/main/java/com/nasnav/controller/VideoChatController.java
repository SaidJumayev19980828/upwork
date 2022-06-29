package com.nasnav.controller;

import com.nasnav.dto.VideoChatLogRepresentationObject;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.VideoChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/videochat")
@CrossOrigin("*")
public class VideoChatController {

    @Autowired
    private VideoChatService videoChatService;

    @GetMapping(value = "/sessions", produces = APPLICATION_JSON_VALUE)
    public List<VideoChatLogRepresentationObject> getOrgSessions(@RequestHeader(name = "User-Token") String userToken,
                                                                 @RequestParam(name = "org_id") Long orgId) {
      return videoChatService.getOrgSessions(orgId);
    }

    @PostMapping(value = "/session", produces = APPLICATION_JSON_VALUE)
    public VideoChatResponse getSession(@RequestHeader(name = "User-Token") String userToken,
                                        @RequestParam(name = "session_name", required = false) String sessionName,
                                        @RequestParam(name = "org_id") Long orgId){
        return videoChatService.getSession(sessionName, orgId);
    }

    @PostMapping(value = "/leave")
    public void leaveSession(@RequestHeader(name = "User-Token") String userToken,
                             @RequestParam("session_name") String sessionName,
                             @RequestParam(name = "org_id") Long orgId) {
        videoChatService.leaveSession(sessionName, orgId);
    }
}