package com.nasnav.controller;

import com.nasnav.request.VideoChatSearchParam;
import com.nasnav.response.VideoChatListResponse;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.VideoChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/videochat")
@CrossOrigin("*")
public class VideoChatController {

    @Autowired
    private VideoChatService videoChatService;

    @GetMapping(value = "/sessions", produces = APPLICATION_JSON_VALUE)
    public VideoChatListResponse getOrgSessions(@RequestHeader(name = "User-Token") String userToken, VideoChatSearchParam params) {
      return videoChatService.getOrgSessions(params);
    }

    @PostMapping(value = "/session", produces = APPLICATION_JSON_VALUE)
    public VideoChatResponse createOrJoinSession(@RequestHeader(name = "User-Token") String userToken,
                                         @RequestParam(name = "session_name", required = false) String sessionName,
                                         @RequestParam(name = "force", required = false, defaultValue = "false") Boolean force,
                                         @RequestParam(name = "org_id", required = false) Long orgId,
                                         @RequestParam(name = "shop_id", required = false) Long shopId){
        return videoChatService.createOrJoinSession(sessionName, force, orgId, shopId);
    }

    @PostMapping(value = "/leave")
    public void leaveSession(@RequestHeader(name = "User-Token") String userToken,
                             @RequestParam("session_name") String sessionName,
                             @RequestParam(name = "org_id", required = false) Long orgId,
                             @RequestParam(name = "shop_id", required = false) Long shopId,
                             @RequestParam(name = "end_call") Boolean endCall) {
        videoChatService.leaveSession(sessionName, orgId, shopId, endCall);
    }
}